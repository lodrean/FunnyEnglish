package com.sotospeak.service.speaking

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

@Suppress("MagicNumber") // тест подписей: байты форматов — суть теста
class AudioSignatureValidatorTest {

    private fun bytes(vararg ints: Int): ByteArray = ByteArray(12) { i -> if (i < ints.size) ints[i].toByte() else 0 }

    @Test
    fun m4aFtypSignatureAccepted() {
        val b = bytes(0, 0, 0, 32, 'f'.code, 't'.code, 'y'.code, 'p'.code, 'M'.code, '4'.code, 'A'.code, ' '.code)
        assertTrue(AudioSignatureValidator.isAllowedAudio(b))
    }

    @Test
    fun mp3AndAadtsSignaturesAccepted() {
        assertTrue(AudioSignatureValidator.isAllowedAudio(bytes('I'.code, 'D'.code, '3'.code, 4, 0)))
        assertTrue(AudioSignatureValidator.isAllowedAudio(bytes(0xFF, 0xFB, 0x90, 0x00)))
        assertTrue(AudioSignatureValidator.isAllowedAudio(bytes(0xFF, 0xF1, 0x50, 0x80)))
    }

    @Test
    fun wavAndOggSignaturesAccepted() {
        assertTrue(
            AudioSignatureValidator.isAllowedAudio(
                bytes('R'.code, 'I'.code, 'F'.code, 'F'.code, 0x24, 0x08, 0, 0, 'W'.code, 'A'.code, 'V'.code, 'E'.code)
            )
        )
        assertTrue(AudioSignatureValidator.isAllowedAudio(bytes('O'.code, 'g'.code, 'g'.code, 'S'.code, 0, 2)))
    }

    @Test
    fun nonAudioContentRejected() {
        assertFalse(AudioSignatureValidator.isAllowedAudio("hello world, not audio".toByteArray()))
        assertFalse(AudioSignatureValidator.isAllowedAudio(bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertFalse(AudioSignatureValidator.isAllowedAudio(byteArrayOf(0xFF.toByte(), 0xFB.toByte())))
    }

    @Test
    fun readHeaderReadsFirstTwelveBytes() {
        val data = ByteArray(64) { it.toByte() }
        val header = AudioSignatureValidator.readHeader(ByteArrayInputStream(data))
        assertTrue(header.size == 12 && header[0].toInt() == 0 && header[11].toInt() == 11)
    }
}
