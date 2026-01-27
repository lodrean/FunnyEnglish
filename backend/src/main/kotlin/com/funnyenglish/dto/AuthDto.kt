package com.funnyenglish.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Display name is required")
    @field:Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
    val displayName: String
)

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class OAuthRequest(
    @field:NotBlank(message = "Provider token is required")
    val token: String,

    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)
