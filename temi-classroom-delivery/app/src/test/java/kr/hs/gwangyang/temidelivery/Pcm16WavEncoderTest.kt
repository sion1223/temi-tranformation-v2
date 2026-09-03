package kr.hs.gwangyang.temidelivery

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Pcm16WavEncoderTest {
    @Test
    fun `encoder writes a mono 16 kHz PCM WAV header and payload`() {
        val pcm = byteArrayOf(1, 2, 3, 4)

        val wav = Pcm16WavEncoder.encode(pcm)

        assertEquals("RIFF", wav.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals(40, littleEndianInt(wav, 4))
        assertEquals("WAVE", wav.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals(1, littleEndianShort(wav, 22))
        assertEquals(16_000, littleEndianInt(wav, 24))
        assertEquals(16, littleEndianShort(wav, 34))
        assertEquals(4, littleEndianInt(wav, 40))
        assertArrayEquals(pcm, wav.copyOfRange(44, 48))
    }

    private fun littleEndianInt(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xff) or
            ((bytes[offset + 1].toInt() and 0xff) shl 8) or
            ((bytes[offset + 2].toInt() and 0xff) shl 16) or
            ((bytes[offset + 3].toInt() and 0xff) shl 24)

    private fun littleEndianShort(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xff) or ((bytes[offset + 1].toInt() and 0xff) shl 8)
}
