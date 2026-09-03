package kr.hs.gwangyang.temidelivery

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

object Pcm16WavEncoder {
    const val SAMPLE_RATE = 16_000
    private const val CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16

    fun encode(pcm: ByteArray): ByteArray {
        require(pcm.isNotEmpty()) { "녹음된 음성이 없습니다." }
        require(pcm.size % 2 == 0) { "PCM 데이터 길이가 올바르지 않습니다." }
        val header = ByteArray(44)
        writeAscii(header, 0, "RIFF")
        writeInt(header, 4, 36 + pcm.size)
        writeAscii(header, 8, "WAVE")
        writeAscii(header, 12, "fmt ")
        writeInt(header, 16, 16)
        writeShort(header, 20, 1)
        writeShort(header, 22, CHANNELS)
        writeInt(header, 24, SAMPLE_RATE)
        writeInt(header, 28, SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8)
        writeShort(header, 32, CHANNELS * BITS_PER_SAMPLE / 8)
        writeShort(header, 34, BITS_PER_SAMPLE)
        writeAscii(header, 36, "data")
        writeInt(header, 40, pcm.size)
        return header + pcm
    }

    private fun writeAscii(target: ByteArray, offset: Int, text: String) {
        text.toByteArray(Charsets.US_ASCII).copyInto(target, offset)
    }

    private fun writeInt(target: ByteArray, offset: Int, value: Int) {
        repeat(4) { index -> target[offset + index] = (value ushr (index * 8)).toByte() }
    }

    private fun writeShort(target: ByteArray, offset: Int, value: Int) {
        repeat(2) { index -> target[offset + index] = (value ushr (index * 8)).toByte() }
    }
}

class Pcm16WavRecorder(
    private val maximumDurationMillis: Long = MAX_VOICE_RECORDING_MILLIS,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) {
    private val recording = AtomicBoolean(false)
    private val lock = Any()
    private var audioRecord: AudioRecord? = null
    private var worker: Thread? = null
    private var output = ByteArrayOutputStream()
    private var workerFailure: Throwable? = null

    val isRecording: Boolean get() = recording.get()

    @SuppressLint("MissingPermission")
    fun start(
        onAutomaticCompletion: (Result<ByteArray>) -> Unit,
    ): Result<Unit> = runCatching {
        synchronized(lock) {
            check(!recording.get()) { "이미 음성을 녹음하고 있습니다." }
            val minimumBuffer = AudioRecord.getMinBufferSize(
                Pcm16WavEncoder.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            check(minimumBuffer > 0) { "마이크 버퍼를 준비하지 못했습니다." }
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                Pcm16WavEncoder.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minimumBuffer.coerceAtLeast(4_096),
            )
            check(recorder.state == AudioRecord.STATE_INITIALIZED) {
                recorder.release()
                "마이크를 초기화하지 못했습니다."
            }
            output = ByteArrayOutputStream()
            workerFailure = null
            audioRecord = recorder
            recording.set(true)
            try {
                recorder.startRecording()
            } catch (error: Throwable) {
                recording.set(false)
                audioRecord = null
                recorder.release()
                throw error
            }
            worker = Thread({
                recordLoop(recorder, minimumBuffer.coerceAtLeast(4_096), onAutomaticCompletion)
            }, "temi-voice-recorder").apply { start() }
        }
    }

    fun stop(): Result<ByteArray> {
        val activeWorker: Thread
        val recorder: AudioRecord
        synchronized(lock) {
            check(recording.get()) { "현재 녹음 중이 아닙니다." }
            recording.set(false)
            activeWorker = checkNotNull(worker)
            recorder = checkNotNull(audioRecord)
        }
        runCatching { recorder.stop() }
        activeWorker.join(2_000)
        return synchronized(lock) {
            workerFailure?.let { Result.failure(it) }
                ?: runCatching { Pcm16WavEncoder.encode(output.toByteArray()) }
        }
    }

    fun cancel() {
        val recorder: AudioRecord?
        synchronized(lock) {
            recording.set(false)
            recorder = audioRecord
        }
        runCatching { recorder?.stop() }
        worker?.join(500)
        synchronized(lock) { output.reset() }
    }

    private fun recordLoop(
        recorder: AudioRecord,
        bufferSize: Int,
        onAutomaticCompletion: (Result<ByteArray>) -> Unit,
    ) {
        val buffer = ByteArray(bufferSize)
        val endOfSpeechDetector = EndOfSpeechDetector()
        val maximumBytes = (Pcm16WavEncoder.SAMPLE_RATE * 2L * maximumDurationMillis / 1_000L)
            .toInt()
        var reachedMaximum = false
        var reachedEndOfSpeech = false
        try {
            while (recording.get()) {
                val remaining = maximumBytes - output.size()
                if (remaining <= 0) {
                    reachedMaximum = true
                    break
                }
                val count = recorder.read(buffer, 0, minOf(buffer.size, remaining))
                when {
                    count > 0 -> {
                        synchronized(lock) { output.write(buffer, 0, count) }
                        val endOfSpeech = endOfSpeechDetector.acceptPcm16(buffer, count)
                        if (endOfSpeech) {
                            reachedEndOfSpeech = true
                            break
                        }
                    }
                    count == AudioRecord.ERROR_INVALID_OPERATION || count == AudioRecord.ERROR_BAD_VALUE -> {
                        if (recording.get()) error("마이크에서 음성을 읽지 못했습니다.")
                    }
                }
            }
            if (output.size() >= maximumBytes) reachedMaximum = true
        } catch (error: Throwable) {
            synchronized(lock) { workerFailure = error }
        } finally {
            recording.set(false)
            runCatching { recorder.stop() }
            recorder.release()
            synchronized(lock) {
                audioRecord = null
                worker = null
            }
        }
        if (reachedMaximum || reachedEndOfSpeech) {
            val result = synchronized(lock) {
                workerFailure?.let { Result.failure(it) }
                    ?: runCatching { Pcm16WavEncoder.encode(output.toByteArray()) }
            }
            mainHandler.post { onAutomaticCompletion(result) }
        } else {
            val failure = synchronized(lock) { workerFailure }
            if (failure != null) mainHandler.post { onAutomaticCompletion(Result.failure(failure)) }
        }
    }

}
