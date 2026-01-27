package com.funnyenglish.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.util.UUID

@Service
class StorageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {
    @Value("\${app.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${app.s3.endpoint}")
    private lateinit var endpoint: String

    fun uploadFile(file: MultipartFile, folder: String): String {
        val extension = file.originalFilename?.substringAfterLast('.', "") ?: ""
        val key = "$folder/${UUID.randomUUID()}.$extension"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromInputStream(file.inputStream, file.size))

        return getPublicUrl(key)
    }

    fun uploadFile(bytes: ByteArray, folder: String, extension: String, contentType: String): String {
        val key = "$folder/${UUID.randomUUID()}.$extension"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromBytes(bytes))

        return getPublicUrl(key)
    }

    fun deleteFile(url: String) {
        val key = extractKeyFromUrl(url) ?: return

        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    fun getPresignedUrl(key: String, expirationMinutes: Long = 60): String {
        val getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .getObjectRequest(getRequest)
            .signatureDuration(Duration.ofMinutes(expirationMinutes))
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    fun ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
        } catch (e: NoSuchBucketException) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())

            // Set public read policy for the bucket
            val policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::$bucket/*"
                        }
                    ]
                }
            """.trimIndent()

            s3Client.putBucketPolicy(
                PutBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .policy(policy)
                    .build()
            )
        }
    }

    private fun getPublicUrl(key: String): String {
        return "$endpoint/$bucket/$key"
    }

    private fun extractKeyFromUrl(url: String): String? {
        val prefix = "$endpoint/$bucket/"
        return if (url.startsWith(prefix)) {
            url.removePrefix(prefix)
        } else {
            null
        }
    }
}
