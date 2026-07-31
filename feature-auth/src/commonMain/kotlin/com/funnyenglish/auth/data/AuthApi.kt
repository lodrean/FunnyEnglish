package com.funnyenglish.auth.data

import com.funnyenglish.core.domain.model.LoginRequest
import com.funnyenglish.core.domain.model.OAuthRequest
import com.funnyenglish.core.domain.model.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): HttpResponse {
        return client.post("/api/auth/login") { setBody(request) }
    }

    suspend fun register(request: RegisterRequest): HttpResponse {
        return client.post("/api/auth/register") { setBody(request) }
    }

    suspend fun oauthLogin(provider: String, request: OAuthRequest): HttpResponse {
        return client.post("/api/auth/oauth/$provider") { setBody(request) }
    }

    suspend fun getCurrentUser(): HttpResponse {
        return client.get("/api/users/me")
    }
}
