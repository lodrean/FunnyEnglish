package com.sotospeak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.unit.DataSize
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Suppress("MagicNumber") // тест подписей: байты форматов — суть теста
class StorageServiceVideoValidationTest {

    private val s3Client: S3Client = mockk(relaxed = true)

    private val mediaProbeService: MediaProbeService = mockk {
        // probe по умолчанию недоступен (fail-open) — видео-кейсы кодеков не срабатывают
        every { probe(any()) } returns null
    }

    private fun storageService(maxVideoSize: String = "100MB") = StorageService(
        s3Client = s3Client,
        bucket = "sotospeak",
        endpoint = "http://localhost:9000",
        publicUrl = "http://localhost:9000",
        mediaProbeService = mediaProbeService,
        maxVideoSize = DataSize.parse(maxVideoSize)
    )

    private fun videoFile(name: String, content: ByteArray, contentType: String = "video/mp4") =
        MockMultipartFile("file", name, contentType, content)

    private val mp4Header =
        byteArrayOf(0, 0, 0, 32, 'f'.code.toByte(), 't'.code.toByte(), 'y'.code.toByte(), 'p'.code.toByte())
    private val ebmlHeader = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte(), 1, 0, 0, 0)

    @Test
    fun validMp4Accepted() {
        val url = storageService().uploadFile(videoFile("clip.mp4", mp4Header + ByteArray(64)), "media")
        assertTrue(url.contains("media/"))
    }

    @Test
    fun validWebmAccepted() {
        val url =
            storageService().uploadFile(videoFile("clip.webm", ebmlHeader + ByteArray(64), "video/webm"), "media")
        assertTrue(url.contains("media/"))
    }

    @Test
    fun fakeVideoExtensionRejectedBeforeUpload() {
        val fake = videoFile("clip.mp4", "renamed text file".toByteArray())
        assertThrows(IllegalArgumentException::class.java) { storageService().uploadFile(fake, "media") }
        verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    fun oversizedVideoRejectedBeforeUpload() {
        val big = videoFile("clip.mp4", mp4Header + ByteArray(1024))
        val service = storageService(maxVideoSize = "1KB")
        assertThrows(IllegalArgumentException::class.java) { service.uploadFile(big, "media") }
        verify(exactly = 0) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    fun unsupportedCodecTranscodedToMp4() {
        every { mediaProbeService.probe(any()) } returns MediaProbeResult("mpeg4", "aac", listOf("mp4"))
        every { mediaProbeService.rejectReason(any(), any()) } returns "Видеокодек 'mpeg4' не поддерживается плеером"
        every { mediaProbeService.transcodeToMp4(any(), any()) } answers {
            val target = secondArg<java.nio.file.Path>()
            java.nio.file.Files.write(target, mp4Header + ByteArray(32))
            true
        }
        val url = storageService().uploadFile(videoFile("clip.mp4", mp4Header + ByteArray(64)), "media")
        assertTrue(url.endsWith(".mp4"))
        verify(exactly = 1) { s3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
    }

    @Test
    fun transcodeFailureRejectsUpload() {
        every { mediaProbeService.probe(any()) } returns MediaProbeResult("mpeg4", null, listOf("mp4"))
        every { mediaProbeService.rejectReason(any(), any()) } returns "Видеокодек 'mpeg4' не поддерживается плеером"
        every { mediaProbeService.transcodeToMp4(any(), any()) } returns false
        val big = videoFile("clip.mp4", mp4Header + ByteArray(64))
        assertThrows(IllegalArgumentException::class.java) { storageService().uploadFile(big, "media") }
    }

    @Test
    fun legacyMovExtensionRejected() {
        val mov = videoFile("clip.mov", mp4Header + ByteArray(64), "video/quicktime")
        assertThrows(IllegalArgumentException::class.java) { storageService().uploadFile(mov, "media") }
    }
}
