package com.funnyenglish.service

import com.funnyenglish.dto.AchievementResponse
import com.funnyenglish.entity.AchievementEntity
import com.funnyenglish.repository.AchievementRepository
import com.funnyenglish.repository.ProgressRepository
import com.funnyenglish.repository.UserAchievementRepository
import com.funnyenglish.shared.model.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

/**
 * Сервис управления достижениями
 */
@Service
class AchievementService(
    private val achievementRepository: AchievementRepository,
    private val userAchievementRepository: UserAchievementRepository,
    private val progressRepository: ProgressRepository
) {
    
    /**
     * Получить все достижения (для авторизованного пользователя - с статусом)
     */
    fun getAllAchievements(userId: String?): List<AchievementResponse> {
        val allAchievements = achievementRepository.findAll()
        
        if (userId == null) {
            return allAchievements.map { it.toResponse() }
        }
        
        val userUUID = UUID.fromString(userId)
        val userAchievements = userAchievementRepository.findByUserId(userUUID)
            .associateBy { it.achievementId }
        
        return allAchievements.map { achievement ->
            val userAchievement = userAchievements[achievement.id]
            achievement.toResponse(userAchievement?.isEarned ?: false)
        }
    }
    
    /**
     * Получить достижения пользователя
     */
    fun getUserAchievements(userId: String): List<AchievementResponse> {
        val userUUID = UUID.fromString(userId)
        val allAchievements = achievementRepository.findAll()
        val userAchievements = userAchievementRepository.findByUserId(userUUID)
            .associateBy { it.achievementId }
        
        return allAchievements.map { achievement ->
            val userAchievement = userAchievements[achievement.id]
            achievement.toResponse(userAchievement?.isEarned ?: false)
        }
    }
    
    /**
     * Получить детали конкретного достижения
     */
    fun getAchievementDetail(userId: UUID, achievementId: String): UserAchievement {
        val achievement = achievementRepository.findById(achievementId)
            .orElseThrow { NoSuchElementException("Achievement not found") }
        
        val userAchievement = userAchievementRepository
            .findByUserIdAndAchievementId(userId, achievementId)
        
        return UserAchievement(
            achievement = mapToAchievementModel(achievement),
            earnedAt = userAchievement?.earnedAt?.toString(),
            progress = userAchievement?.progress ?: 0f,
            isEarned = userAchievement?.isEarned ?: false
        )
    }
    
    /**
     * Получить статистику достижений
     */
    fun getAchievementStats(userId: UUID): com.funnyenglish.controller.AchievementStats {
        val userAchievements = userAchievementRepository.findByUserId(userId)
        val earnedCount = userAchievements.count { it.isEarned }
        val totalCount = achievementRepository.count()
        
        val achievements = getUserAchievements(userId.toString())
        val categoryProgress = AchievementCategory.entries.associate { category ->
            val categoryAchievements = achievements.filter { 
                val entity = achievementRepository.findById(it.id).orElse(null)
                entity != null && entity.category == category.name
            }
            val earnedInCategory = categoryAchievements.count { it.earned }
            val percentage = if (categoryAchievements.isNotEmpty()) {
                (earnedInCategory * 100) / categoryAchievements.size
            } else 0
            
            category to com.funnyenglish.controller.CategoryStat(
                earned = earnedInCategory,
                total = categoryAchievements.size,
                percentage = percentage
            )
        }
        
        return com.funnyenglish.controller.AchievementStats(
            totalEarned = earnedCount,
            totalAvailable = totalCount.toInt(),
            categoryProgress = categoryProgress
        )
    }
    
    /**
     * Проверить и разблокировать достижения при событии
     */
    fun checkAchievements(userId: UUID, event: GameEvent): List<Achievement> {
        val unlockedAchievements = mutableListOf<Achievement>()
        val allAchievements = achievementRepository.findAll()
        val userAchievements = userAchievementRepository.findByUserId(userId)
            .associateBy { it.achievementId }
        
        allAchievements.forEach { achievement ->
            val userAchievement = userAchievements[achievement.id]
            
            // Skip if already earned
            if (userAchievement?.isEarned == true) return@forEach
            
            // Check if condition met
            val conditionType = try {
                ConditionType.valueOf(achievement.conditionType)
            } catch (e: IllegalArgumentException) {
                // Unknown condition type, skip this achievement
                return@forEach
            }
            
            val condition = AchievementCondition(
                type = conditionType,
                targetValue = achievement.conditionTarget
            )
            val conditionMet = checkCondition(condition, event, userId)
            
            if (conditionMet) {
                // Award achievement
                awardAchievement(userId, achievement)
                unlockedAchievements.add(mapToAchievementModel(achievement))
            }
        }
        
        return unlockedAchievements
    }
    
    /**
     * Check and award achievements after test completion (used by ProgressService)
     */
    fun checkAndAwardAchievements(userId: String, percentage: Int, stars: Int): List<AchievementResponse> {
        val userUUID = UUID.fromString(userId)
        val event = GameEvent.LessonCompleted(
            lessonId = "",
            score = 0,
            maxScore = 100,
            percentage = percentage,
            timeSpent = 0,
            timestamp = Instant.now().toString()
        )
        val newAchievements = checkAchievements(userUUID, event)
        return newAchievements.map { 
            val entity = achievementRepository.findById(it.id).orElse(null)
            entity?.toResponse(true) ?: AchievementResponse(
                id = it.id,
                code = it.code,
                name = it.name,
                description = it.description,
                iconUrl = it.iconUrl,
                pointsReward = it.pointsReward,
                earned = true
            )
        }
    }
    
    /**
     * Обновить прогресс достижения
     */
    fun updateProgress(userId: UUID, achievementId: String, progress: Float) {
        val userAchievement = userAchievementRepository
            .findByUserIdAndAchievementId(userId, achievementId)
        
        if (userAchievement == null) {
            val achievement = achievementRepository.findById(achievementId)
                .orElseThrow { NoSuchElementException("Achievement not found") }
            
            val newUserAchievement = com.funnyenglish.entity.UserAchievementEntity(
                id = UUID.randomUUID(),
                userId = userId,
                achievementId = achievementId,
                progress = progress.coerceIn(0f, 1f),
                isEarned = progress >= 1f,
                earnedAt = if (progress >= 1f) Instant.now() else null
            )
            userAchievementRepository.save(newUserAchievement)
        } else {
            val newProgress = progress.coerceIn(0f, 1f)
            userAchievement.progress = newProgress
            if (newProgress >= 1f && !userAchievement.isEarned) {
                userAchievement.isEarned = true
                userAchievement.earnedAt = Instant.now()
            }
            userAchievementRepository.save(userAchievement)
        }
    }
    
    // ==================== Private Methods ====================
    
    private fun AchievementEntity.toResponse(earned: Boolean = false): AchievementResponse {
        return AchievementResponse(
            id = id,
            code = code,
            name = name,
            description = description,
            iconUrl = iconUrl,
            pointsReward = pointsReward,
            earned = earned
        )
    }
    
    private fun mapToAchievementModel(entity: AchievementEntity): Achievement {
        val category = try {
            AchievementCategory.valueOf(entity.category)
        } catch (e: IllegalArgumentException) {
            AchievementCategory.EXPLORER // Default category
        }
        
        val rarity = try {
            Rarity.valueOf(entity.rarity)
        } catch (e: IllegalArgumentException) {
            Rarity.COMMON // Default rarity
        }
        
        val conditionType = try {
            ConditionType.valueOf(entity.conditionType)
        } catch (e: IllegalArgumentException) {
            ConditionType.TESTS_COMPLETED // Default condition
        }
        
        return Achievement(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            description = entity.description,
            iconUrl = entity.iconUrl,
            category = category,
            rarity = rarity,
            isHidden = entity.isHidden,
            condition = AchievementCondition(
                type = conditionType,
                targetValue = entity.conditionTarget
            ),
            pointsReward = entity.pointsReward
        )
    }
    
    private fun checkCondition(
        condition: AchievementCondition,
        event: GameEvent,
        userId: UUID
    ): Boolean {
        return when (condition.type) {
            ConditionType.LESSONS_COMPLETED -> {
                if (event is GameEvent.LessonCompleted) {
                    val count = userAchievementRepository.countLessonsCompleted(userId)
                    count >= condition.targetValue
                } else false
            }
            
            ConditionType.STREAK_DAYS -> {
                if (event is GameEvent.StreakActivity) {
                    event.streakDay >= condition.targetValue
                } else false
            }
            
            ConditionType.PERFECT_LESSONS, ConditionType.PERFECT_TESTS -> {
                if (event is GameEvent.LessonCompleted) {
                    event.percentage == 100
                } else false
            }
            
            ConditionType.ALL_EXERCISE_TYPES -> {
                // Check if user has tried all exercise types
                val triedTypes = userAchievementRepository.getTriedExerciseTypes(userId)
                triedTypes.size >= condition.targetValue
            }
            
            ConditionType.EARLY_BIRD -> {
                // Check if lesson completed before 8 AM
                val hour = java.time.LocalDateTime.now().hour
                hour < 8
            }
            
            ConditionType.NIGHT_OWL -> {
                // Check if lesson completed after 10 PM
                val hour = java.time.LocalDateTime.now().hour
                hour >= 22
            }
            
            ConditionType.TESTS_COMPLETED -> {
                if (event is GameEvent.LessonCompleted) {
                    val count = progressRepository.countByUserId(userId)
                    count >= condition.targetValue
                } else false
            }
            
            else -> false // TODO: Implement other conditions
        }
    }
    
    private fun awardAchievement(userId: UUID, achievement: AchievementEntity) {
        val userAchievement = com.funnyenglish.entity.UserAchievementEntity(
            id = UUID.randomUUID(),
            userId = userId,
            achievementId = achievement.id,
            progress = 1f,
            isEarned = true,
            earnedAt = Instant.now()
        )
        userAchievementRepository.save(userAchievement)
        
        // TODO: Send notification
        // TODO: Award XP
    }
}
