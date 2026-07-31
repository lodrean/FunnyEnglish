package com.funnyenglish.auth.domain

import com.funnyenglish.core.domain.model.AuthResponse
import com.funnyenglish.core.domain.model.LoginRequest
import com.funnyenglish.core.domain.model.MergeGuestProgressRequest
import com.funnyenglish.core.domain.model.MergeGuestProgressResponse
import com.funnyenglish.core.domain.model.OAuthRequest
import com.funnyenglish.core.domain.model.RegisterRequest
import com.funnyenglish.core.domain.model.User
import com.funnyenglish.core.domain.util.Result
import com.funnyenglish.core.domain.util.DataError

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse, DataError.Network>
    suspend fun register(request: RegisterRequest): Result<AuthResponse, DataError.Network>
    suspend fun oauthLogin(provider: String, request: OAuthRequest): Result<AuthResponse, DataError.Network>
    suspend fun getCurrentUser(): Result<User, DataError.Network>
    suspend fun mergeGuestProgress(request: MergeGuestProgressRequest): Result<MergeGuestProgressResponse, DataError.Network>
}
