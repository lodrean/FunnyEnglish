package com.sotospeak.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            fieldName to (error.defaultMessage ?: "Invalid value")
        }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "Validation failed",
                message = "Invalid request data",
                details = errors
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                error = "Bad request",
                message = ex.message ?: "Invalid request"
            )
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                error = "Not found",
                message = ex.message ?: "Resource not found"
            )
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                error = "Forbidden",
                message = "Access denied"
            )
        )
    }

    @ExceptionHandler(com.sotospeak.service.EmailNotVerifiedException::class)
    fun handleEmailNotVerified(ex: com.sotospeak.service.EmailNotVerifiedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                error = "EMAIL_NOT_VERIFIED",
                message = "Подтвердите email по ссылке из письма"
            )
        )
    }

    @ExceptionHandler(com.sotospeak.exception.DuplicateSubmissionException::class)
    fun handleDuplicateSubmission(ex: com.sotospeak.exception.DuplicateSubmissionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                error = "DUPLICATE_SUBMISSION",
                message = ex.message ?: "Submission already exists"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        // Stack trace только в логах, НЕ в ответе клиенту (security: не палим внутренности)
        logger.error("Internal server error: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                error = "Internal server error",
                message = "An unexpected error occurred"
            )
        )
    }
}

data class ErrorResponse(
    val error: String,
    val message: String,
    val details: Map<String, String>? = null
)
