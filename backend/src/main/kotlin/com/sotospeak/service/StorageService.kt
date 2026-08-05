package com.sotospeak.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.slf4j.LoggerFactory
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
    @Value("\${app.s3.endpoint}") private val endpoint: String,
    @Value("\${app.s3.public-url}") private val publicUrl: String
) {
    private val logger = LoggerFactory.getLogger(StorageService::class.java)
    private val allowedImageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    private val allowedAudioExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac")
    private val allowedVideoExtensions = setOf("mp4", "webm", "mov", "m4v")
    private val allowedSubtitleExtensions = setOf("vtt")

    fun uploadFile(file: MultipartFile, folder: String): String {
        logger.info("=".repeat(50))
        logger.info("UPLOAD START: originalName=${file.originalFilename}, size=${file.size}, contentType=${file.contentType}, folder=$folder")
        logger.info("S3 Config: endpoint=$endpoint, bucket=$bucket, publicUrl=$publicUrl")
        
        val normalizedFolder = folder.trim().trim('/').ifEmpty { "media" }
        val originalName = file.originalFilename?.trim().orEmpty()
        val safeFileName = originalName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()
            .ifEmpty { "file" }
        val extension = safeFileName.substringAfterLast('.', "").lowercase()
        
        logger.debug("Normalized folder: $normalizedFolder, safeFileName: $safeFileName, extension: $extension")
        
        validateFileType(extension, file.contentType)
        
        val key = buildString {
            append(normalizedFolder)
            append('/')
            append(UUID.randomUUID())
            if (extension.isNotEmpty()) {
                append('.')
                append(extension.lowercase())
            }
        }
        
        logger.info("Uploading to S3: bucket=$bucket, key=$key")

        try {
            val request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType ?: "application/octet-stream")
                .contentLength(file.size)
                .build()

            file.inputStream.use { input ->
                s3Client.putObject(request, RequestBody.fromInputStream(input, file.size))
            }
            
            val url = buildObjectUrl(key)
            logger.info("UPLOAD SUCCESS: $url")
            logger.info("=".repeat(50))
            return url
        } catch (e: Exception) {
            logger.error("Failed to upload file to S3: bucket=$bucket, key=$key", e)
            throw IllegalStateException("Failed to upload file: ${e.message}", e)
        }
    }

    fun deleteFile(url: String) {
        val key = extractKey(url) ?: return
        val request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        runCatching {
            s3Client.deleteObject(request)
        }.onFailure { error ->
            logger.warn("Failed to delete object from S3 for url={}", url, error)
            throw IllegalStateException("Unable to delete file")
        }
    }

    private fun buildObjectUrl(key: String): String {
        val baseUrl = publicUrl.trimEnd('/')
        if (baseUrl.isEmpty()) {
            return "/$bucket/$key"
        }
        val uri = runCatching { URI(baseUrl) }.getOrNull()
        val host = uri?.host
        val pathSegments = uri?.path
            ?.trim('/')
            ?.split('/')
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val hasBucketInHost = host?.startsWith("$bucket.") == true
        val hasBucketInPath = pathSegments.lastOrNull() == bucket

        return if (hasBucketInHost || hasBucketInPath) {
            "$baseUrl/$key"
        } else {
            "$baseUrl/$bucket/$key"
        }
    }

    private fun extractKey(url: String): String? {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            return null
        }
        val uri = runCatching { URI(trimmedUrl) }.getOrNull()
        val rawPath = uri?.path ?: trimmedUrl
        val decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8)
        val normalizedPath = decodedPath.trim('/')
        if (normalizedPath.isEmpty()) {
            return null
        }

        val hostHasBucket = uri?.host?.startsWith("$bucket.") == true
        val cleanedPath = when {
            hostHasBucket && normalizedPath.startsWith("$bucket/") ->
                normalizedPath.removePrefix("$bucket/").trimStart('/')
            normalizedPath.startsWith("$bucket/") ->
                normalizedPath.removePrefix("$bucket/").trimStart('/')
            else -> normalizedPath
        }

        return cleanedPath.trim('/').ifEmpty { null }
    }

    private fun validateFileType(extension: String, contentType: String?) {
        if (extension.isBlank()) {
            throw IllegalArgumentException("File extension is required")
        }

        val isImage = extension in allowedImageExtensions
        val isAudio = extension in allowedAudioExtensions
        val isVideo = extension in allowedVideoExtensions
        val isSubtitle = extension in allowedSubtitleExtensions
        if (!isImage && !isAudio && !isVideo && !isSubtitle) {
            throw IllegalArgumentException("Unsupported file type: .$extension")
        }

        val normalizedContentType = contentType?.lowercase().orEmpty()
        if (normalizedContentType.isNotEmpty() && normalizedContentType != "application/octet-stream") {
            val isContentImage = normalizedContentType.startsWith("image/")
            val isContentAudio = normalizedContentType.startsWith("audio/")
            val isContentVideo = normalizedContentType.startsWith("video/")
            val isContentSubtitle = normalizedContentType == "text/vtt" || normalizedContentType.startsWith("text/plain")
            if (!isContentImage && !isContentAudio && !isContentVideo && !isContentSubtitle) {
                throw IllegalArgumentException("Unsupported content type: $normalizedContentType")
            }
            if (isContentImage && !isImage) {
                throw IllegalArgumentException("File extension does not match content type")
            }
            if (isContentAudio && !isAudio) {
                throw IllegalArgumentException("File extension does not match content type")
            }
            if (isContentVideo && !isVideo) {
                throw IllegalArgumentException("File extension does not match content type")
            }
            if (isContentSubtitle && !isSubtitle) {
                throw IllegalArgumentException("File extension does not match content type")
            }
        }
    }
}
