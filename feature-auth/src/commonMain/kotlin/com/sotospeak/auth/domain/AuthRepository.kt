package com.sotospeak.auth.domain

import com.sotospeak.core.domain.model.AuthResponse
import com.sotospeak.core.domain.model.LoginRequest
import com.sotospeak.core.domain.model.MergeGuestProgressRequest
import com.sotospeak.core.domain.model.MergeGuestProgressResponse
import com.sotospeak.core.domain.model.OAuthRequest
import com.sotospeak.core.domain.model.RegisterRequest
import com.sotospeak.core.domain.model.User
import com.sotospeak.core.domain.util.Result
import com.sotospeak.core.domain.util.DataError

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse, DataError.Network>
    suspend fun register(request: RegisterRequest): Result<AuthResponse, DataError.Network>
    suspend fun oauthLogin(provider: String, request: OAuthRequest): Result<AuthResponse, DataError.Network>
    suspend fun getCurrentUser(): Result<User, DataError.Network>
    suspend fun mergeGuestProgress(request: MergeGuestProgressRequest): Result<MergeGuestProgressResponse, DataError.Network>
}
