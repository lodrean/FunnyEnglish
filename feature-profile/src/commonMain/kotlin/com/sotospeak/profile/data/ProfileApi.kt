package com.sotospeak.profile.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class ProfileApi(private val client: HttpClient) {

    suspend fun getUserProfile(): HttpResponse {
        return client.get("/api/users/me/profile")
    }

    suspend fun getProgressSummary(): HttpResponse {
        return client.get("/api/users/me/progress/summary")
    }

    suspend fun getAllAchievements(): HttpResponse {
        return client.get("/api/achievements")
    }
}
