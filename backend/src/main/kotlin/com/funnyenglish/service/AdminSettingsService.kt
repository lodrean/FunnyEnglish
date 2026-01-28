package com.funnyenglish.service

import com.funnyenglish.dto.AdminSettingsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AdminSettingsService(
    @Value("\${app.s3.endpoint}") private val s3Endpoint: String,
    @Value("\${app.s3.bucket}") private val s3Bucket: String,
    @Value("\${app.s3.region}") private val s3Region: String,
    @Value("\${spring.servlet.multipart.max-file-size:}") private val maxFileSize: String,
    @Value("\${spring.servlet.multipart.max-request-size:}") private val maxRequestSize: String,
    @Value("\${app.cors.allowed-origins:}") private val corsAllowedOrigins: String
) {
    fun getSettings(): AdminSettingsResponse {
        val origins = corsAllowedOrigins.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return AdminSettingsResponse(
            s3Endpoint = s3Endpoint,
            s3Bucket = s3Bucket,
            s3Region = s3Region,
            maxFileSize = maxFileSize,
            maxRequestSize = maxRequestSize,
            corsAllowedOrigins = origins
        )
    }
}
