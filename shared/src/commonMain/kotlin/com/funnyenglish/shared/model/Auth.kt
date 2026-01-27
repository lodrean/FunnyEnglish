package com.funnyenglish.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class OAuthRequest(
    val token: String,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: Map<String, String>? = null
)
