package kr.hs.gwangyang.temidelivery

import kotlin.math.sqrt

internal data class EndOfSpeechConfig(
    val sampleRate: Int = Pcm16WavEncoder.SAMPLE_RATE,
    val warmupMillis: Long = 300,
    val minimumSpeechMillis: Long = 240,
    val trailingSilenceMillis: Long = 1_500,
    val speechStartRms: Double = 800.0,
    val speechContinueRms: Double = 1_000.0,
    val speechStartNoiseMultiplier: Double = 3.0,
    val speechContinueNoiseMultiplier: Double = 2.0,
    val maximumCalibratedNoiseRms: Double = 400.0,
) {
    init {
        require(sampleRate > 0)
        require(warmupMillis >= 0)
        require(minimumSpeechMillis > 0)
        require(trailingSilenceMillis > 0)
        require(speechStartRms > 0)
        require(speechContinueRms > 0)
        require(speechStartNoiseMultiplier > 1.0)
        require(speechContinueNoiseMultiplier > 1.0)
        require(maximumCalibratedNoiseRms > 0)
    }
}

/**
 * Lightweight energy-based VAD for the foreground PCM recorder.
 *
 * It deliberately waits until sustained speech has been observed, so a quiet user who has not
 * started speaking is still protected by the recorder's 20-second hard limit instead of being
 * cut off as silence. Once armed, 1.5 seconds of trailing silence completes the utterance.
 */
internal class EndOfSpeechDetector(
    private val config: EndOfSpeechConfig = EndOfSpeechConfig(),
) {
    private val warmupSamples = config.millisToSamples(config.warmupMillis)
    private val minimumSpeechSamples = config.millisToSamples(config.minimumSpeechMillis)
    private val trailingSilenceSamples = config.millisToSamples(config.trailingSilenceMillis)

    private var observedSamples = 0L
    private val warmupRmsLevels = mutableListOf<Double>()
    private var candidateSpeechSamples = 0L
    private var silentSamples = 0L
    private var speechDetected = false
    private var completed = false
    private var calibrated = false
    private var speechStartThreshold = config.speechStartRms
    private var speechContinueThreshold = config.speechContinueRms
    private var calibratedNoiseRms = 0.0

    fun acceptPcm16(buffer: ByteArray, byteCount: Int = buffer.size): Boolean {
        require(byteCount in 0..buffer.size)
        val sampleCount = byteCount / BYTES_PER_SAMPLE
        if (sampleCount == 0 || completed) return completed

        val rms = calculateRms(buffer, sampleCount)
        observedSamples += sampleCount
        if (observedSamples <= warmupSamples) {
            warmupRmsLevels += rms
            return false
        }
        calibrateThresholdsIfNeeded()

        if (!speechDetected) {
            candidateSpeechSamples = if (rms >= speechStartThreshold) {
                candidateSpeechSamples + sampleCount
            } else {
                0
            }
            if (candidateSpeechSamples >= minimumSpeechSamples) {
                speechDetected = true
                silentSamples = 0
            }
            return false
        }

        silentSamples = if (rms >= speechContinueThreshold) 0 else silentSamples + sampleCount
        completed = silentSamples >= trailingSilenceSamples
        return completed
    }

    private fun calculateRms(buffer: ByteArray, sampleCount: Int): Double {
        var squaredSum = 0.0
        repeat(sampleCount) { index ->
            val byteOffset = index * BYTES_PER_SAMPLE
            val sample = (
                (buffer[byteOffset].toInt() and 0xff) or
                    (buffer[byteOffset + 1].toInt() shl 8)
                ).toShort().toDouble()
            squaredSum += sample * sample
        }
        return sqrt(squaredSum / sampleCount)
    }

    private fun calibrateThresholdsIfNeeded() {
        if (calibrated) return
        calibrated = true
        if (warmupRmsLevels.isEmpty()) return
        val sorted = warmupRmsLevels.sorted()
        calibratedNoiseRms = sorted[sorted.size / 2].coerceAtMost(config.maximumCalibratedNoiseRms)
        speechStartThreshold = maxOf(
            config.speechStartRms,
            calibratedNoiseRms * config.speechStartNoiseMultiplier,
        )
        speechContinueThreshold = maxOf(
            config.speechContinueRms,
            calibratedNoiseRms * config.speechContinueNoiseMultiplier,
        )
    }

    private fun EndOfSpeechConfig.millisToSamples(durationMillis: Long): Long =
        sampleRate.toLong() * durationMillis / 1_000L

    private companion object {
        const val BYTES_PER_SAMPLE = 2
    }
}
