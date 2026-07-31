package com.funnyenglish.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MediaUrlService(
    @Value("\${app.s3.endpoint}") private val endpoint: String,
    @Value("\${app.s3.public-url}") private val publicUrl: String
) {
    fun normalize(url: String?): String? {
        if (url.isNullOrBlank()) {
            return url
        }

        val trimmed = url.trim()
        val publicBase = publicUrl.trimEnd('/')
        if (publicBase.isEmpty()) {
            return trimmed
        }

        if (trimmed.startsWith("/")) {
            return "$publicBase$trimmed"
        }

        val endpointBase = endpoint.trimEnd('/')
        if (endpointBase.isNotEmpty() && trimmed.startsWith(endpointBase)) {
            val suffix = trimmed.removePrefix(endpointBase)
            return "$publicBase$suffix"
        }

        return trimmed
    }
}
