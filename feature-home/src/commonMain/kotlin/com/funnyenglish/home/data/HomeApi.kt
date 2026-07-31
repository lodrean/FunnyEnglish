package com.funnyenglish.home.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class HomeApi(private val client: HttpClient) {

    suspend fun getUserProfile(): HttpResponse {
        return client.get("/api/users/me/profile")
    }

    suspend fun getCategories(): HttpResponse {
        return client.get("/api/categories")
    }

    suspend fun getAllTests(): HttpResponse {
        return client.get("/api/tests")
    }
}
