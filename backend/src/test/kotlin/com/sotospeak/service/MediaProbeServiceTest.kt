package com.sotospeak.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

@Suppress("MagicNumber")
class MediaProbeServiceTest {

    private val service = MediaProbeService(ffprobePath = "ffprobe-definitely-missing", probeTimeoutSeconds = 1)

    @Test
    fun parsesFfprobeJsonDump() {
        val json = """
        {
          "streams": [
            {"codec_type": "video", "codec_name": "h264", "width": 1280},
            {"codec_type": "audio", "codec_name": "aac"}
          ],
          "format": {"format_name": "mov,mp4,m4a,3gp,3g2,mj2", "duration": "60.0"}
        }
        """.trimIndent()
        val result = service.parse(json)
        assertNotNull(result)
        assertEquals("h264", result!!.videoCodec)
        assertEquals("aac", result.audioCodec)
        assertTrue(result.containerFormats.contains("mp4"))
    }

    @Test
    fun invalidJsonReturnsNull() {
        assertNull(service.parse("not a json"))
    }

    @Test
    fun missingStreamsReturnsNull() {
        assertNull(service.parse("{}"))
    }

    @Test
    fun h264InMp4ContainerAllowed() {
        val result = MediaProbeResult("h264", "aac", listOf("mov", "mp4"))
        assertNull(service.rejectReason("mp4", result))
    }

    @Test
    fun vp9InWebmContainerAllowed() {
        val result = MediaProbeResult("vp9", "opus", listOf("matroska", "webm"))
        assertNull(service.rejectReason("webm", result))
    }

    @Test
    fun exoticCodecRejected() {
        val result = MediaProbeResult("mpeg4", "aac", listOf("mp4"))
        val reason = service.rejectReason("mp4", result)
        assertTrue(reason!!.contains("mpeg4"))
        assertTrue(reason.contains("перекодируйте"))
    }

    @Test
    fun codecMismatchWithContainerRejected() {
        val result = MediaProbeResult("vp9", "opus", listOf("matroska", "webm"))
        val reason = service.rejectReason("mp4", result)
        assertTrue(reason!!.contains("vp9"))
    }

    @Test
    fun fileWithoutVideoStreamRejected() {
        val result = MediaProbeResult(null, "aac", listOf("mp4"))
        assertTrue(service.rejectReason("mp4", result)!!.contains("нет видеопотока"))
    }

    @Test
    fun probeWithMissingBinaryReturnsNull() {
        val temp = Files.createTempFile("probe_test_", ".mp4")
        try {
            Files.write(temp, byteArrayOf(1, 2, 3))
            assertNull(service.probe(temp), "нет ffprobe в PATH → fail-open")
        } finally {
            Files.deleteIfExists(temp)
        }
    }
}
