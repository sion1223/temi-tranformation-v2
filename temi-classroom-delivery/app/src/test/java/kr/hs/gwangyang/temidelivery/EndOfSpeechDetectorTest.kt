package kr.hs.gwangyang.temidelivery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndOfSpeechDetectorTest {
    private val config = EndOfSpeechConfig(
        sampleRate = 1_000,
        warmupMillis = 300,
        minimumSpeechMillis = 240,
        trailingSilenceMillis = 1_500,
        speechStartRms = 220.0,
        speechContinueRms = 160.0,
    )

    @Test
    fun `silence alone never completes a recording`() {
        val detector = EndOfSpeechDetector(config)

        repeat(30) {
            assertFalse(detector.acceptPcm16(frame(amplitude = 40, samples = 100)))
        }
    }

    @Test
    fun `speech followed by trailing silence completes the recording`() {
        val detector = EndOfSpeechDetector(config)

        repeat(3) { assertFalse(detector.acceptPcm16(frame(40, 100))) }
        repeat(3) { assertFalse(detector.acceptPcm16(frame(1_200, 100))) }
        repeat(14) { assertFalse(detector.acceptPcm16(frame(40, 100))) }

        assertTrue(detector.acceptPcm16(frame(40, 100)))
    }

    @Test
    fun `brief noise does not arm automatic completion`() {
        val detector = EndOfSpeechDetector(config)

        repeat(3) { assertFalse(detector.acceptPcm16(frame(40, 100))) }
        assertFalse(detector.acceptPcm16(frame(1_200, 100)))
        repeat(25) { assertFalse(detector.acceptPcm16(frame(40, 100))) }
    }

    @Test
    fun `calibrated room noise is treated as trailing silence`() {
        val detector = EndOfSpeechDetector(config)

        repeat(3) { assertFalse(detector.acceptPcm16(frame(300, 100))) }
        repeat(3) { assertFalse(detector.acceptPcm16(frame(1_200, 100))) }
        repeat(14) { assertFalse(detector.acceptPcm16(frame(300, 100))) }

        assertTrue(detector.acceptPcm16(frame(300, 100)))
    }

    @Test
    fun `temi microphone noise does not arm but nearby speech completes`() {
        val detector = EndOfSpeechDetector(EndOfSpeechConfig(sampleRate = 1_000))

        repeat(3) { assertFalse(detector.acceptPcm16(frame(100, 100))) }
        repeat(20) { assertFalse(detector.acceptPcm16(frame(500, 100))) }
        repeat(3) { assertFalse(detector.acceptPcm16(frame(1_800, 100))) }
        repeat(14) { assertFalse(detector.acceptPcm16(frame(500, 100))) }

        assertTrue(detector.acceptPcm16(frame(500, 100)))
    }

    @Test
    fun `speech resumes before timeout and resets trailing silence`() {
        val detector = EndOfSpeechDetector(config)

        repeat(3) { detector.acceptPcm16(frame(40, 100)) }
        repeat(3) { detector.acceptPcm16(frame(1_200, 100)) }
        repeat(10) { assertFalse(detector.acceptPcm16(frame(40, 100))) }
        assertFalse(detector.acceptPcm16(frame(1_200, 100)))
        repeat(14) { assertFalse(detector.acceptPcm16(frame(40, 100))) }

        assertTrue(detector.acceptPcm16(frame(40, 100)))
    }

    private fun frame(amplitude: Int, samples: Int): ByteArray = ByteArray(samples * 2).also { pcm ->
        repeat(samples) { index ->
            pcm[index * 2] = (amplitude and 0xff).toByte()
            pcm[index * 2 + 1] = (amplitude ushr 8).toByte()
        }
    }
}
