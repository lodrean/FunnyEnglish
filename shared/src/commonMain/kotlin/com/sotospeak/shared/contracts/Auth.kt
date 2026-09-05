package com.sotospeak.shared.contracts

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

/**
 * Пара токенов (nj2.7): access — Bearer на API; refresh — одноразовый, для
 * /auth/refresh (ротация при каждом обмене). Nullable + default: старый backend
 * (до nj2.7) поле не отдаёт — клиент не должен падать на десериализации.
 */
@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: User
)

/** Ответ регистрации: при включённой email-верификации токена нет (login после подтверждения почты). */
@Serializable
data class RegisterResponse(
    val user: User,
    val emailSent: Boolean = false,
    val token: String? = null,
    val refreshToken: String? = null
)

@Serializable
data class ResendVerificationRequest(
    val email: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: Map<String, String>? = null
)
