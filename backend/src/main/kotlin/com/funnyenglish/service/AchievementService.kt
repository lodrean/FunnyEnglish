package com.funnyenglish.service

import com.funnyenglish.dto.AchievementResponse
import com.funnyenglish.dto.toResponse
import com.funnyenglish.entity.Achievement
import com.funnyenglish.entity.User
import com.funnyenglish.repository.AchievementRepository
import com.funnyenglish.repository.ProgressRepository
import com.funnyenglish.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AchievementService(
    private val achievementRepository: AchievementRepository,
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository
) {
    fun getAllAchievements(userId: String?): List<AchievementResponse> {
        val allAchievements = achievementRepository.findVisibleAchievements()

        val earnedIds = if (userId != null) {
            val userUUID = UUID.fromString(userId)
            achievementRepository.findByUserId(userUUID).map { it.id }.toSet()
        } else {
            emptySet()
        }

        return allAchievements.map { achievement ->
            achievement.toResponse(earned = achievement.id in earnedIds)
        }
    }

    fun getUserAchievements(userId: String): List<AchievementResponse> {
        val userUUID = UUID.fromString(userId)
        return achievementRepository.findByUserId(userUUID).map { it.toResponse(earned = true) }
    }

    @Transactional
    fun checkAndAwardAchievements(userId: String, lastTestPercentage: Int, lastTestStars: Int): List<AchievementResponse> {
        val userUUID = UUID.fromString(userId)
        val user = userRepository.findById(userUUID).orElseThrow { NoSuchElementException("User not found") }
        val earnedAchievements = user.achievements.map { it.code }.toMutableSet()
        val newAchievements = mutableListOf<Achievement>()

        val testsCompleted = progressRepository.countByUserId(userUUID)
        val perfectScores = progressRepository.countPerfectScoresByUserId(userUUID)

        // FIRST_TEST - Complete first test
        if ("FIRST_TEST" !in earnedAchievements && testsCompleted >= 1) {
            achievementRepository.findByCode("FIRST_TEST")?.let {
                newAchievements.add(it)
            }
        }

        // PERFECT_SCORE - Get 100% on any test
        if ("PERFECT_SCORE" !in earnedAchievements && lastTestPercentage == 100) {
            achievementRepository.findByCode("PERFECT_SCORE")?.let {
                newAchievements.add(it)
            }
        }

        // STREAK_3 - 3 day streak
        if ("STREAK_3" !in earnedAchievements && user.currentStreak >= 3) {
            achievementRepository.findByCode("STREAK_3")?.let {
                newAchievements.add(it)
            }
        }

        // STREAK_7 - 7 day streak
        if ("STREAK_7" !in earnedAchievements && user.currentStreak >= 7) {
            achievementRepository.findByCode("STREAK_7")?.let {
                newAchievements.add(it)
            }
        }

        // STREAK_30 - 30 day streak
        if ("STREAK_30" !in earnedAchievements && user.currentStreak >= 30) {
            achievementRepository.findByCode("STREAK_30")?.let {
                newAchievements.add(it)
            }
        }

        // TESTS_10 - Complete 10 tests
        if ("TESTS_10" !in earnedAchievements && testsCompleted >= 10) {
            achievementRepository.findByCode("TESTS_10")?.let {
                newAchievements.add(it)
            }
        }

        // TESTS_50 - Complete 50 tests
        if ("TESTS_50" !in earnedAchievements && testsCompleted >= 50) {
            achievementRepository.findByCode("TESTS_50")?.let {
                newAchievements.add(it)
            }
        }

        // Award new achievements
        if (newAchievements.isNotEmpty()) {
            user.achievements.addAll(newAchievements)
            userRepository.save(user)

            // Add bonus points for achievements
            val bonusPoints = newAchievements.sumOf { it.pointsReward }
            if (bonusPoints > 0) {
                userRepository.save(
                    user.copy(totalPoints = user.totalPoints + bonusPoints)
                )
            }
        }

        return newAchievements.map { it.toResponse(earned = true) }
    }
}
