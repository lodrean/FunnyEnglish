package com.funnyenglish.data.remote

import com.funnyenglish.shared.model.Achievement
import com.funnyenglish.shared.model.DailyQuest
import com.funnyenglish.shared.model.LeaderboardEntry
import com.funnyenglish.shared.model.StreakData
import com.funnyenglish.shared.model.StreakUpdateResult
import com.funnyenglish.shared.model.WeeklyQuest
import com.funnyenglish.shared.model.XpData
import com.funnyenglish.shared.model.XpGain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * FunnyEnglish API Client
 * 
 * Ktor HTTP client for backend communication
 * Base URL: http://localhost:8080/api/v1
 */

class FunnyEnglishApi(
    private val baseUrl: String = "http://localhost:8080/api",
    private val authTokenProvider: () -> String? = { null }
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            level = LogLevel.ALL
        }
        
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            authTokenProvider()?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
        
        HttpResponseValidator {
            validateResponse { response ->
                when (response.status) {
                    HttpStatusCode.Unauthorized -> throw ApiException.Unauthorized()
                    HttpStatusCode.NotFound -> throw ApiException.NotFound()
                    HttpStatusCode.BadRequest -> throw ApiException.BadRequest()
                    else -> if (!response.status.isSuccess()) {
                        throw ApiException.Unknown(response.status.value)
                    }
                }
            }
        }
    }
    
    // ==================== Streak API ====================
    
    suspend fun getStreakData(): StreakData {
        return client.get("/api/gamification/streak").body()
    }
    
    suspend fun recordActivity(): StreakUpdateResult {
        return client.post("/api/gamification/streak/record").body()
    }
    
    suspend fun recoverStreak(challengeId: String): RecoveryResponse {
        return client.post("/api/gamification/streak/recovery/$challengeId").body()
    }
    
    // ==================== XP API ====================
    
    suspend fun getXpData(): XpData {
        return client.get("/api/gamification/xp").body()
    }
    
    suspend fun getXpHistory(limit: Int = 10): List<XpGain> {
        return client.get("/api/gamification/xp/history") {
            url { parameters.append("limit", limit.toString()) }
        }.body()
    }
    
    // ==================== Quests API ====================
    
    suspend fun getDailyQuests(): List<DailyQuest> {
        return client.get("/api/gamification/quests/daily").body()
    }
    
    suspend fun getWeeklyQuests(): List<WeeklyQuest> {
        return client.get("/api/gamification/quests/weekly").body()
    }
    
    suspend fun claimQuestReward(questId: String): ClaimRewardResponse {
        return client.post("/api/gamification/quests/$questId/claim").body()
    }
    
    // ==================== Achievements API ====================
    
    suspend fun getAchievements(): List<Achievement> {
        return client.get("/api/gamification/achievements").body()
    }
    
    suspend fun getAchievementDetail(achievementId: String): Achievement {
        return client.get("/api/gamification/achievements/$achievementId").body()
    }
    
    // ==================== Leaderboard API ====================
    
    suspend fun getLeaderboard(limit: Int = 10, scope: String = "global"): LeaderboardResponse {
        return client.get("/api/gamification/leaderboard") {
            url {
                parameters.append("limit", limit.toString())
                parameters.append("scope", scope)
            }
        }.body()
    }
}

// ==================== Response DTOs ====================

@kotlinx.serialization.Serializable
data class RecoveryResponse(
    val success: Boolean,
    val message: String = ""
)

@kotlinx.serialization.Serializable
data class ClaimRewardResponse(
    val xpEarned: Int,
    val gemsEarned: Int,
    val newAchievements: List<Achievement>
)

@kotlinx.serialization.Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>,
    val userRank: Int?,
    val scope: String
)

// ==================== API Exceptions ====================

sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized : ApiException("Unauthorized")
    class NotFound : ApiException("Not found")
    class BadRequest : ApiException("Bad request")
    class Unknown(code: Int) : ApiException("HTTP $code")
}
