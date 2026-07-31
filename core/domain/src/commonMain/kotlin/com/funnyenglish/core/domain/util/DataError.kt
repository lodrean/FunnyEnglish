package com.funnyenglish.core.domain.util

/**
 * Concrete error types used across the application.
 */
sealed interface DataError : DomainError {

    enum class Network : DataError {
        REQUEST_TIMEOUT,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}
