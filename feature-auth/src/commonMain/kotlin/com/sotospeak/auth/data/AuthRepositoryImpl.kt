package com.sotospeak.auth.data

import com.sotospeak.auth.domain.AuthRepository
import com.sotospeak.core.data.network.safeCall
import com.sotospeak.core.domain.model.AuthResponse
import com.sotospeak.core.domain.model.LoginRequest
import com.sotospeak.core.domain.model.MergeGuestProgressRequest
import com.sotospeak.core.domain.model.MergeGuestProgressResponse
import com.sotospeak.core.domain.model.OAuthRequest
import com.sotospeak.core.domain.model.RegisterRequest
import com.sotospeak.core.domain.model.User
import com.sotospeak.core.domain.util.DataError
import com.sotospeak.core.domain.util.Result
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
