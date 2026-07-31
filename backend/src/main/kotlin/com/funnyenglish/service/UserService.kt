package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.Progress
import com.funnyenglish.entity.User
import com.funnyenglish.repository.AchievementRepository
import com.funnyenglish.repository.GuestEventRepository
import com.funnyenglish.repository.ProgressRepository
import com.funnyenglish.repository.TestRepository
import com.funnyenglish.repository.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val progressRepository: ProgressRepository,
    private val achievementRepository: AchievementRepository,
    private val achievementService: AchievementService,
    private val testRepository: TestRepository,
    private val guestEventRepository: GuestEventRepository
) {
    companion object {
        val LEVEL_THRESHOLDS = listOf(
            0 to "Новичок",
            100 to "Ученик",
            300 to "Знаток",
            600 to "Мастер",
            1000 to "Эксперт",
            1500 to "Профессионал",
            2500 to "Гуру",
            4000 to "Легенда"
        )
    }

    fun getUserById(userId: String): User {
        return userRepository.findById(UUID.fromString(userId))
            .orElseThrow { NoSuchElementException("User not found") }
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAllByOrderByCreatedAtDesc()
    }

    fun getUserStats(user: User): UserStats {
        val testsCompleted = progressRepository.countByUserId(user.id)
        val totalStars = progressRepository.sumStarsByUserId(user.id) ?: 0
        val perfectScores = progressRepository.countPerfectScoresByUserId(user.id)
        val (currentLevel, pointsToNext) = calculateLevelInfo(user.totalPoints)

        return UserStats(
            testsCompleted = testsCompleted,
            totalStars = totalStars,
            perfectScores = perfectScores,
            currentLevel = currentLevel,
            pointsToNextLevel = pointsToNext
        )
    }

    @Cacheable(value = ["userProfiles"], key = "#userId")
    fun getUserProfile(userId: String): UserProfileResponse {
        val user = getUserById(userId)

        val achievements = achievementService.getUserAchievements(userId)

        return UserProfileResponse(
            user = user.toResponse(),
            stats = getUserStats(user),
            achievements = achievements
        )
    }

    @Transactional
    @CacheEvict(value = ["userProfiles"], key = "#userId")
    fun addPoints(userId: String, points: Int): Pair<User, LevelUpInfo?> {
        val user = getUserById(userId)
        val oldLevel = calculateLevel(user.totalPoints)

        val updatedUser = user.copy(
            totalPoints = user.totalPoints + points,
            updatedAt = Instant.now()
        )

        val newLevel = calculateLevel(updatedUser.totalPoints)
        val savedUser = userRepository.save(
            if (newLevel > oldLevel) {
                updatedUser.copy(level = newLevel)
            } else {
                updatedUser
            }
        )

        val levelUpInfo = if (newLevel > oldLevel) {
            LevelUpInfo(
                previousLevel = oldLevel,
                newLevel = newLevel,
                newTitle = getLevelTitle(newLevel)
            )
        } else null

        return savedUser to levelUpInfo
    }

    @Transactional
    @CacheEvict(value = ["userProfiles"], key = "#userId")
    fun updateStreak(userId: String): User {
        val user = getUserById(userId)
        val today = Instant.now()

        val lastActivity = user.lastActivityDate
        val newStreak = if (lastActivity == null) {
            1
        } else {
            val daysSinceLastActivity = java.time.Duration.between(lastActivity, today).toDays()
            when {
                daysSinceLastActivity == 0L -> user.currentStreak
                daysSinceLastActivity == 1L -> user.currentStreak + 1
                else -> 1
            }
        }

        return userRepository.save(
            user.copy(
                currentStreak = newStreak,
                lastActivityDate = today,
                updatedAt = today
            )
        )
    }

    @Transactional
    @CacheEvict(value = ["userProfiles"], key = "#userId")
    fun mergeGuestProgress(
        userId: String,
        request: MergeGuestProgressRequest
    ): MergeGuestProgressResponse {
        val user = getUserById(userId)
        var totalXpAdded = 0
        var mergedTests = 0
        val allNewAchievements = mutableListOf<AchievementResponse>()
        var levelUp: LevelUpInfo? = null

        // Метрика конверсии: помечаем обезличенные события гостя как конвертированные
        request.anonymousId?.let { anonId ->
            runCatching { UUID.fromString(anonId) }.getOrNull()?.let { anonUUID ->
                guestEventRepository.markConverted(anonUUID, user.id)
            }
        }

        for (guestProgress in request.testProgress) {
            val testUUID = UUID.fromString(guestProgress.testId)
            val test = testRepository.findById(testUUID).orElse(null) ?: continue

            // Security validation
            if (guestProgress.score > guestProgress.maxScore || guestProgress.maxScore <= 0) {
                continue
            }

            val existingProgress = progressRepository.findByUserIdAndTestId(user.id, testUUID)

            val isNewBestScore = existingProgress == null || guestProgress.score > existingProgress.bestScore

            if (!isNewBestScore) {
                // No improvement, skip
                continue
            }

            val percentage = guestProgress.maxScore.let {
                if (it > 0) (guestProgress.score * 100) / it else 0
            }
            val stars = when {
                percentage >= 95 -> 3
                percentage >= 80 -> 2
                percentage >= 60 -> 1
                else -> 0
            }

            val progress = if (existingProgress != null) {
                existingProgress.copy(
                    score = guestProgress.score,
                    maxScore = guestProgress.maxScore,
                    stars = maxOf(existingProgress.stars, stars),
                    attemptsCount = existingProgress.attemptsCount + 1,
                    bestScore = maxOf(existingProgress.bestScore, guestProgress.score),
                    timeSpentSeconds = guestProgress.timeSpentSeconds,
                    lastAttemptAt = Instant.now()
                )
            } else {
                Progress(
                    user = user,
                    test = test,
                    score = guestProgress.score,
                    maxScore = guestProgress.maxScore,
                    stars = stars,
                    bestScore = guestProgress.score,
                    timeSpentSeconds = guestProgress.timeSpentSeconds
                )
            }

            progressRepository.save(progress)
            mergedTests++

            // Calculate XP for this merge
            val pointsEarned = if (existingProgress == null) {
                test.pointsReward + (stars * 5)
            } else {
                // Only award difference for improvement
                val oldXp = test.pointsReward + (existingProgress.stars * 5)
                val newXp = test.pointsReward + (stars * 5)
                maxOf(0, newXp - oldXp)
            }

            totalXpAdded += pointsEarned

            // Check achievements for this test
            val newAchievements = achievementService.checkAndAwardAchievements(userId, percentage, stars)
            allNewAchievements.addAll(newAchievements)
        }

        if (totalXpAdded > 0) {
            val (updatedUser, userLevelUp) = addPoints(userId, totalXpAdded)
            levelUp = userLevelUp
        }

        if (mergedTests > 0) {
            updateStreak(userId)
        }

        return MergeGuestProgressResponse(
            mergedTests = mergedTests,
            totalXpAdded = totalXpAdded,
            newAchievements = allNewAchievements.distinctBy { it.id },
            levelUp = levelUp
        )
    }

    @Cacheable(value = ["leaderboard"], key = "(#currentUserId ?: 'anonymous') + '-' + #limit")
    fun getLeaderboard(currentUserId: String?, limit: Int = 10): LeaderboardResponse {
        val topUsers = userRepository.findTopByTotalPoints(limit)

        val entries = topUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                rank = index + 1,
                userId = user.id.toString(),
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                level = user.level,
                totalPoints = user.totalPoints
            )
        }

        var userRank: Int? = null
        var userAbove: LeaderboardEntry? = null
        var userBelow: LeaderboardEntry? = null

        if (currentUserId != null) {
            val userUUID = UUID.fromString(currentUserId)
            val currentUser = userRepository.findById(userUUID).orElse(null)

            if (currentUser != null) {
                // Find user's rank
                userRank = entries.indexOfFirst { it.userId == currentUserId }
                    .takeIf { it >= 0 }
                    ?.let { it + 1 }

                if (userRank == null) {
                    // User not in top, calculate approximate rank
                    userRank = topUsers.count { it.totalPoints > currentUser.totalPoints } + 1
                }

                // Get users above and below
                userRepository.findUserAbove(userUUID)?.let { above ->
                    userAbove = LeaderboardEntry(
                        rank = userRank - 1,
                        userId = above.id.toString(),
                        displayName = above.displayName,
                        avatarUrl = above.avatarUrl,
                        level = above.level,
                        totalPoints = above.totalPoints
                    )
                }

                userRepository.findUserBelow(userUUID)?.let { below ->
                    userBelow = LeaderboardEntry(
                        rank = userRank + 1,
                        userId = below.id.toString(),
                        displayName = below.displayName,
                        avatarUrl = below.avatarUrl,
                        level = below.level,
                        totalPoints = below.totalPoints
                    )
                }
            }
        }

        return LeaderboardResponse(
            entries = entries,
            userRank = userRank,
            usersAbove = userAbove,
            usersBelow = userBelow
        )
    }

    private fun calculateLevel(points: Int): Int {
        for (i in LEVEL_THRESHOLDS.indices.reversed()) {
            if (points >= LEVEL_THRESHOLDS[i].first) {
                return i + 1
            }
        }
        return 1
    }

    private fun calculateLevelInfo(points: Int): Pair<Int, Int> {
        val level = calculateLevel(points)
        val nextLevelThreshold = LEVEL_THRESHOLDS.getOrNull(level)?.first ?: Int.MAX_VALUE
        return level to (nextLevelThreshold - points).coerceAtLeast(0)
    }

    private fun getLevelTitle(level: Int): String {
        return LEVEL_THRESHOLDS.getOrNull(level - 1)?.second ?: "Легенда"
    }
}
