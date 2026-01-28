package com.funnyenglish.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class StorageService(
    private val s3Client: S3Client,
    @Value("\${app.s3.bucket}") private val bucket: String,
    @Value("\${app.s3.endpoint}") private val endpoint: String
) {
    fun uploadFile(file: MultipartFile, folder: String): String {
        val normalizedFolder = folder.trim().trim('/').ifEmpty { "media" }
        val originalName = file.originalFilename?.trim().orEmpty().ifEmpty { "file" }
        val extension = originalName.substringAfterLast('.', "")
        val key = buildString {
            append(normalizedFolder)
            append('/')
            append(UUID.randomUUID())
            if (extension.isNotEmpty()) {
                append('.')
                append(extension.lowercase())
            }
        }

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.contentType ?: "application/octet-stream")
            .contentLength(file.size)
            .build()

        file.inputStream.use { input ->
            s3Client.putObject(request, RequestBody.fromInputStream(input, file.size))
        }

        return buildObjectUrl(key)
    }

    fun deleteFile(url: String) {
        val key = extractKey(url) ?: return
        val request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(request)
    }

    private fun buildObjectUrl(key: String): String {
        return "${endpoint.trimEnd('/')}/$bucket/$key"
    }

    private fun extractKey(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull()
        val rawPath = uri?.path ?: url
        val decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8)
        val path = decodedPath.trimStart('/')

        if (path.startsWith("$bucket/")) {
            return path.removePrefix("$bucket/")
        }

        if (uri?.host?.startsWith("$bucket.") == true) {
            return path.ifEmpty { null }
        }

        return if (path == url.trim()) url.trim().ifEmpty { null } else path.ifEmpty { null }
    }
}
