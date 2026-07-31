package com.funnyenglish.auth.data

import com.funnyenglish.auth.domain.AuthRepository
import com.funnyenglish.core.data.network.safeCall
import com.funnyenglish.core.domain.model.AuthResponse
import com.funnyenglish.core.domain.model.LoginRequest
import com.funnyenglish.core.domain.model.MergeGuestProgressRequest
import com.funnyenglish.core.domain.model.MergeGuestProgressResponse
import com.funnyenglish.core.domain.model.OAuthRequest
import com.funnyenglish.core.domain.model.RegisterRequest
import com.funnyenglish.core.domain.model.User
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val client: HttpClient
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse, DataError.Network> {
        return safeCall { api.login(request) }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse, DataError.Network> {
        return safeCall { api.register(request) }
    }

    override suspend fun oauthLogin(
        provider: String,
        request: OAuthRequest
    ): Result<AuthResponse, DataError.Network> {
        return safeCall { api.oauthLogin(provider, request) }
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        return safeCall { api.getCurrentUser() }
    }

    override suspend fun mergeGuestProgress(
        request: MergeGuestProgressRequest
    ): Result<MergeGuestProgressResponse, DataError.Network> {
        return safeCall {
            client.post("/api/users/me/merge-guest-progress") { setBody(request) }
        }
    }
}
