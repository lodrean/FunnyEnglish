package com.sotospeak.shared.api

import com.sotospeak.shared.model.AuthResponse
import com.sotospeak.shared.model.LoginRequest
import com.sotospeak.shared.model.OAuthRequest
import com.sotospeak.shared.model.RegisterRequest
import com.sotospeak.shared.model.RegisterResponse
import com.sotospeak.shared.model.User
import com.sotospeak.shared.model.UserProfile

/**
 * Срез API: аутентификация и текущий пользователь.
 * Умеренный разбор монолита [SoToSpeakApi] (bd FunnyEnglish-5tf.5, К4 §2.2 PROJECT-REVIEW-2026-08-28):
 * VM зависят от узкого интерфейса, а не от конкретного Ktor-клиента.
 */
interface AuthApi {
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun resendVerification(email: String): Result<Unit>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun oauthLogin(provider: String, request: OAuthRequest): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<User>
    suspend fun getUserProfile(): Result<UserProfile>
}
