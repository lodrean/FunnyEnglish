package com.sotospeak.service

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("MagicNumber") // тест подписей: байты форматов — суть теста
class VideoSignatureValidatorTest {

    private fun bytes(vararg ints: Int): ByteArray = ByteArray(12) { i -> if (i < ints.size) ints[i].toByte() else 0 }

    @Test
    fun mp4FtypSignatureAccepted() {
        val b = bytes(0, 0, 0, 32, 'f'.code, 't'.code, 'y'.code, 'p'.code, 'i'.code, 's'.code, 'o'.code, 'm'.code)
        assertTrue(VideoSignatureValidator.isAllowedVideo(b))
    }

    @Test
    fun webmEbmlSignatureAccepted() {
        assertTrue(VideoSignatureValidator.isAllowedVideo(bytes(0x1A, 0x45, 0xDF, 0xA3, 1, 0, 0, 0)))
    }

    @Test
    fun nonVideoContentRejected() {
        assertFalse(VideoSignatureValidator.isAllowedVideo("hello world, not video".toByteArray()))
        assertFalse(VideoSignatureValidator.isAllowedVideo(bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)))
        assertFalse(VideoSignatureValidator.isAllowedVideo(bytes('R'.code, 'I'.code, 'F'.code, 'F'.code)))
        assertFalse(VideoSignatureValidator.isAllowedVideo(bytes('O'.code, 'g'.code, 'g'.code, 'S'.code)))
    }

    @Test
    fun truncatedHeaderRejected() {
        assertFalse(VideoSignatureValidator.isAllowedVideo(byteArrayOf(0x1A, 0x45)))
        assertFalse(VideoSignatureValidator.isAllowedVideo(ByteArray(0)))
    }

    @Test
    fun readHeaderReadsAtLeastTwelveBytes() {
        val data = ByteArray(64) { (it + 1).toByte() }
        val header = VideoSignatureValidator.readHeader(data.inputStream())
        assertTrue(header.size >= 12)
    }
}
