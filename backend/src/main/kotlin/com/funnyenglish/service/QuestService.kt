package com.funnyenglish.service

import com.funnyenglish.repository.QuestRepository
import com.funnyenglish.shared.model.*
import org.springframework.stereotype.Service
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*

/**
 * Сервис управления квестами
 */
@Service
class QuestService(
    private val questRepository: QuestRepository,
    private val xpService: XpService
) {
    
    companion object {
        const val DAILY_QUESTS_COUNT = 3
        const val WEEKLY_QUESTS_COUNT = 2
    }
    
    /**
     * Получить ежедневные квесты пользователя
     */
    fun getDailyQuests(userId: UUID): List<DailyQuest> {
        // Check if quests exist for today
        val existingQuests = questRepository.findDailyQuestsForToday(userId)
        
        if (existingQuests.isNotEmpty()) {
            return existingQuests.map { it.toModel() }
        }
        
        // Generate new quests
        val newQuests = generateDailyQuests(userId)
        newQuests.forEach { questRepository.save(it) }
        
        return newQuests.map { it.toModel() }
    }
    
    /**
     * Получить еженедельные квесты
     */
    fun getWeeklyQuests(userId: UUID): List<WeeklyQuest> {
        // Calculate week boundaries
        val now = Instant.now()
        val weekStart = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        val weekEnd = LocalDate.now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
        
        val existingQuests = questRepository.findWeeklyQuestsForCurrentWeek(userId, weekStart, weekEnd)
        
        if (existingQuests.isNotEmpty()) {
            return existingQuests.map { it.toWeeklyQuest() }
        }
        
        val newQuests = generateWeeklyQuests(userId)
        newQuests.forEach { questRepository.save(it) }
        
        return newQuests.map { it.toWeeklyQuest() }
    }
    
    /**
     * Забрать награду за квест
     */
    fun claimReward(userId: UUID, questId: String): QuestReward {
        val quest = questRepository.findById(UUID.fromString(questId))
            .orElseThrow { NoSuchElementException("Quest not found") }
        
        if (quest.userId != userId) {
            throw IllegalAccessException("Not your quest")
        }
        
        if (!quest.isCompleted) {
            throw IllegalStateException("Quest not completed")
        }
        
        if (quest.isRewardClaimed) {
            throw IllegalStateException("Reward already claimed")
        }
        
        // Mark as claimed
        quest.isRewardClaimed = true
        questRepository.save(quest)
        
        // Award XP
        xpService.addXp(
            userId = userId,
            amount = quest.rewardXp,
            source = XpSource.QUEST_COMPLETED,
            description = "Completed quest: ${quest.title}"
        )
        
        return QuestReward(
            xp = quest.rewardXp,
            gems = quest.rewardGems
        )
    }
    
    /**
     * Обновить прогресс квеста
     */
    fun updateQuestProgress(userId: UUID, type: QuestType, increment: Int = 1) {
        val activeQuests = questRepository.findActiveQuestsByType(userId, type)
        
        activeQuests.forEach { quest ->
            if (!quest.isCompleted) {
                quest.currentValue += increment
                if (quest.currentValue >= quest.targetValue) {
                    quest.currentValue = quest.targetValue
                    quest.isCompleted = true
                    quest.completedAt = Instant.now()
                    
                    // TODO: Send notification
                }
                questRepository.save(quest)
            }
        }
    }
    
    /**
     * Получить время сброса ежедневных квестов
     */
    fun getDailyResetTime(): Instant {
        return LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    }
    
    /**
     * Получить время сброса еженедельных квестов
     */
    fun getWeeklyResetTime(): Instant {
        return LocalDate.now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    }
    
    // ==================== Private Methods ====================
    
    private fun generateDailyQuests(userId: UUID): List<com.funnyenglish.entity.Quest> {
        val quests = mutableListOf<com.funnyenglish.entity.Quest>()
        
        // Easy quest
        quests.add(createQuest(
            userId = userId,
            title = "Быстрая тренировка",
            description = "Завершите 1 урок",
            type = QuestType.COMPLETE_LESSONS,
            target = 1,
            difficulty = QuestDifficulty.EASY,
            rewardXp = 15,
            rewardGems = 3
        ))
        
        // Medium quest
        quests.add(createQuest(
            userId = userId,
            title = "Ежедневная практика",
            description = "Заработайте 50 XP",
            type = QuestType.EARN_XP,
            target = 50,
            difficulty = QuestDifficulty.MEDIUM,
            rewardXp = 25,
            rewardGems = 5
        ))
        
        // Hard quest (varies)
        val hardQuestType = listOf(
            QuestType.PERFECT_SCORE to ("Идеальный результат" to "Получите 100% в тесте"),
            QuestType.PRACTICE_STREAK to ("Поддержите серию" to "Занимайтесь 3 дня подряд"),
            QuestType.REVIEW_WORDS to ("Повторение" to "Повторите 10 слов")
        ).random()
        
        quests.add(createQuest(
            userId = userId,
            title = hardQuestType.second.first,
            description = hardQuestType.second.second,
            type = hardQuestType.first,
            target = if (hardQuestType.first == QuestType.REVIEW_WORDS) 10 else 1,
            difficulty = QuestDifficulty.HARD,
            rewardXp = 50,
            rewardGems = 10
        ))
        
        return quests
    }
    
    private fun generateWeeklyQuests(userId: UUID): List<com.funnyenglish.entity.Quest> {
        val quests = mutableListOf<com.funnyenglish.entity.Quest>()
        
        quests.add(createQuest(
            userId = userId,
            title = "Недельный марафон",
            description = "Завершите 10 уроков за неделю",
            type = QuestType.COMPLETE_LESSONS,
            target = 10,
            difficulty = QuestDifficulty.MEDIUM,
            rewardXp = 100,
            rewardGems = 20
        ))
        
        quests.add(createQuest(
            userId = userId,
            title = "Мастер произношения",
            description = "Потренируйтесь с произношением 5 раз",
            type = QuestType.PRACTICE_PRONUNCIATION,
            target = 5,
            difficulty = QuestDifficulty.HARD,
            rewardXp = 150,
            rewardGems = 30
        ))
        
        return quests
    }
    
    private fun createQuest(
        userId: UUID,
        title: String,
        description: String,
        type: QuestType,
        target: Int,
        difficulty: QuestDifficulty,
        rewardXp: Int,
        rewardGems: Int
    ): com.funnyenglish.entity.Quest {
        return com.funnyenglish.entity.Quest(
            id = UUID.randomUUID(),
            userId = userId,
            title = title,
            description = description,
            questType = type.name,
            targetValue = target,
            currentValue = 0,
            rewardXp = rewardXp,
            rewardGems = rewardGems,
            isCompleted = false,
            isRewardClaimed = false,
            createdAt = Instant.now(),
            expiresAt = getDailyResetTime()
        )
    }
}

// ==================== Extension Functions ====================

private fun com.funnyenglish.entity.Quest.toModel(): DailyQuest {
    return DailyQuest(
        id = this.id.toString(),
        title = this.title,
        description = this.description,
        type = QuestType.valueOf(this.questType),
        targetValue = this.targetValue,
        currentValue = this.currentValue,
        reward = QuestReward(
            xp = this.rewardXp,
            gems = this.rewardGems
        ),
        expiresAt = this.expiresAt.toString(),
        isCompleted = this.isCompleted,
        difficulty = QuestDifficulty.MEDIUM // TODO: Store in entity
    )
}

private fun com.funnyenglish.entity.Quest.toWeeklyQuest(): WeeklyQuest {
    return WeeklyQuest(
        id = this.id.toString(),
        title = this.title,
        description = this.description,
        objectives = listOf(
            QuestObjective(
                type = QuestType.valueOf(this.questType),
                target = this.targetValue,
                current = this.currentValue,
                description = this.description
            )
        ),
        reward = QuestReward(
            xp = this.rewardXp,
            gems = this.rewardGems
        ),
        expiresAt = this.expiresAt.toString(),
        isCompleted = this.isCompleted
    )
}
