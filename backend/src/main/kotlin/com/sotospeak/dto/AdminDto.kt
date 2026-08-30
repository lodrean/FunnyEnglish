package com.sotospeak.dto

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AdminAnalyticsResponse(
    val totalUsers: Long,
    val totalTests: Long,
    val publishedTests: Long,
    val totalQuestions: Long,
    val totalAnswers: Long,
    val totalCompletions: Long,
    val totalCategories: Long,
    val totalAchievements: Long,
    val topCategories: List<CategoryCompletionResponse>
)

data class AdminSettingsResponse(
    val s3Endpoint: String,
    val s3Bucket: String,
    val s3Region: String,
    val maxFileSize: String,
    val maxRequestSize: String,
    val corsAllowedOrigins: List<String>
)

data class CategoryCompletionResponse(
    val categoryId: UUID,
    val categoryName: String,
    val completions: Long
)

data class DailyActivityResponse(
    val date: LocalDate,
    val newUsers: Int,
    val testsCompleted: Int,
    val achievementsEarned: Int
)

data class LevelDistributionResponse(
    val level: Int,
    val users: Long
)

data class PopularTestResponse(
    val id: String,
    val name: String,
    val completions: Int,
    val category: String
)

data class RecentActivityResponse(
    val type: String,      // NEW_USER, TEST_COMPLETED, ACHIEVEMENT
    val userName: String,
    val details: String?,
    val timestamp: Instant
)

/** Метрики PRD (Speaking Trainer §Metrics, bd FunnyEnglish-h3l.3) — агрегаты по реальным данным. */
data class PrdMetricsResponse(
    /** Practice-отправок за последние 7 дней */
    val practiceSubmissionsLast7d: Long,
    /** Учеников с хотя бы одной отправкой за последние 7 дней */
    val activeStudentsLast7d: Long,
    /** Practice-отправок на ученика в неделю (0, если отправок нет) */
    val practicePerStudentPerWeek: Double,
    /** Всего отправок, получивших оценку (REVIEWED) */
    val reviewedTotal: Long,
    /** Из них оценено в течение 48 часов после отправки */
    val reviewedWithin48h: Long,
    /** Доля REVIEWED за 48ч, 0..1 (0, если оценённых нет) */
    val reviewedWithin48hShare: Double,
    /** Всего уникальных гостей (по anonymousId) */
    val totalGuests: Long,
    /** Гостей, конвертировавшихся в регистрацию */
    val convertedGuests: Long,
    /** Конверсия гость → регистрация, 0..1 */
    val guestConversionRate: Double
)

data class GuestAnalyticsResponse(
    /** Всего уникальных гостей (по anonymousId) */
    val totalGuests: Long,
    /** Гостей с событиями за последние 7 дней */
    val activeGuests7d: Long,
    /** Событий прохождения тестов гостями */
    val guestTestCompletions: Long,
    /** Гостей, конвертировавшихся в регистрацию */
    val convertedGuests: Long,
    /** Конверсия в регистрацию, 0..1 */
    val conversionRate: Double
)
