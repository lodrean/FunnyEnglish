package com.funnyenglish.app.di

import com.funnyenglish.app.storage.RecordingKind
import com.funnyenglish.app.storage.RecordingMeta
import com.funnyenglish.app.viewmodel.TopicUiModel
import com.funnyenglish.shared.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Мок-реализации для тестирования.
 * Используются shared модели из com.funnyenglish.shared.model
 */

// ============================================
// USER MOCKS
// ============================================

val mockUser = User(
    id = "user-1",
    email = "test@example.com",
    displayName = "Test User",
    avatarUrl = null,
    level = 5,
    totalPoints = 1250,
    currentStreak = 7,
    role = "USER",
    createdAt = "2024-01-10T10:00:00Z"
)

val mockUserStats = UserStats(
    testsCompleted = 15,
    totalStars = 25,
    perfectScores = 3,
    currentLevel = 5,
    pointsToNextLevel = 250
)

val mockUserProfile = UserProfile(
    user = mockUser,
    stats = mockUserStats,
    achievements = emptyList()
)

// ============================================
// CATEGORY MOCKS
// ============================================

val mockCategories = listOf(
    Category(
        id = "cat-1",
        name = "Grammar",
        description = "Grammar tests",
        iconUrl = null,
        testsCount = 10,
        completedCount = 3,
        totalStars = 8
    ),
    Category(
        id = "cat-2",
        name = "Vocabulary",
        description = "Vocabulary tests",
        iconUrl = null,
        testsCount = 15,
        completedCount = 5,
        totalStars = 12
    ),
    Category(
        id = "cat-3",
        name = "Listening",
        description = "Listening tests",
        iconUrl = null,
        testsCount = 8,
        completedCount = 2,
        totalStars = 5
    )
)

// ============================================
// TEST LIST MOCKS
// ============================================

val mockTestListItems = listOf(
    TestListItem(
        id = "test-1",
        categoryId = "cat-1",
        title = "Present Simple",
        description = "Test your knowledge of Present Simple tense",
        thumbnailUrl = null,
        difficulty = Difficulty.EASY,
        pointsReward = 50,
        questionsCount = 5,
        userProgress = TestProgressSummary(
            completed = true,
            bestScore = 80,
            maxScore = 100,
            stars = 2
        )
    ),
    TestListItem(
        id = "test-2",
        categoryId = "cat-1",
        title = "Past Tense",
        description = "Learn past tense forms",
        thumbnailUrl = null,
        difficulty = Difficulty.MEDIUM,
        pointsReward = 75,
        questionsCount = 5,
        userProgress = null
    ),
    TestListItem(
        id = "test-3",
        categoryId = "cat-2",
        title = "Food Vocabulary",
        description = "Food and restaurant vocabulary",
        thumbnailUrl = null,
        difficulty = Difficulty.EASY,
        pointsReward = 50,
        questionsCount = 10,
        userProgress = null
    )
)

// ============================================
// QUESTION MOCKS - TEXT_SELECT
// ============================================

val mockTextSelectQuestions = listOf(
    Question(
        id = "q-1",
        type = QuestionType.TEXT_SELECT,
        title = null,
        text = "I _____ to school every day.",
        audioUrl = null,
        imageUrl = null,
        points = 10,
        answers = listOf(
            Answer(id = "a-1", text = "go", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-2", text = "goes", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-3", text = "going", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-4", text = "gone", imageUrl = null, audioUrl = null, matchTarget = null)
        ),
        imageWordMatchContent = null
    ),
    Question(
        id = "q-2",
        type = QuestionType.TEXT_SELECT,
        title = null,
        text = "She _____ coffee in the morning.",
        audioUrl = null,
        imageUrl = null,
        points = 10,
        answers = listOf(
            Answer(id = "a-5", text = "drink", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-6", text = "drinks", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-7", text = "drinking", imageUrl = null, audioUrl = null, matchTarget = null),
            Answer(id = "a-8", text = "drunk", imageUrl = null, audioUrl = null, matchTarget = null)
        ),
        imageWordMatchContent = null
    )
)

// ============================================
// QUESTION MOCKS - IMAGE_WORD_MATCH
// ============================================

val mockImageWordMatchQuestions = listOf(
    Question(
        id = "q-iwm-1",
        type = QuestionType.IMAGE_WORD_MATCH,
        title = "Match the words to the objects",
        text = "Drag the words to the correct objects on the image",
        audioUrl = null,
        imageUrl = "https://via.placeholder.com/800x600/4A90D9/FFFFFF?text=Room+Objects",
        points = 10,
        answers = emptyList(),
        imageWordMatchContent = ImageWordMatchContent(
            id = "iwm-1",
            type = "image_word_match",
            points = 10,
            imageUrl = "https://via.placeholder.com/800x600/4A90D9/FFFFFF?text=Room+Objects",
            instruction = "Drag the words to the correct objects on the image",
            words = listOf(
                WordData(id = "word-1", text = "door", translation = "дверь", audioUrl = null),
                WordData(id = "word-2", text = "window", translation = "окно", audioUrl = null),
                WordData(id = "word-3", text = "table", translation = "стол", audioUrl = null),
                WordData(id = "word-4", text = "chair", translation = "стул", audioUrl = null)
            ),
            hotspots = listOf(
                HotspotData(id = "hs-1", x = 0.1f, y = 0.2f, width = 0.15f, height = 0.3f, shape = HotspotShape.RECTANGLE, wordId = null),
                HotspotData(id = "hs-2", x = 0.7f, y = 0.1f, width = 0.25f, height = 0.25f, shape = HotspotShape.RECTANGLE, wordId = null),
                HotspotData(id = "hs-3", x = 0.4f, y = 0.5f, width = 0.2f, height = 0.2f, shape = HotspotShape.RECTANGLE, wordId = null),
                HotspotData(id = "hs-4", x = 0.65f, y = 0.55f, width = 0.15f, height = 0.2f, shape = HotspotShape.RECTANGLE, wordId = null)
            )
        )
    )
)

// ============================================
// TEST DETAIL MOCKS
// ============================================

val mockTestDetail = TestDetail(
    id = "test-1",
    categoryId = "cat-1",
    title = "Present Simple",
    description = "Test your knowledge of Present Simple tense",
    thumbnailUrl = null,
    difficulty = Difficulty.EASY,
    pointsReward = 50,
    timeLimitSeconds = null,
    questions = mockTextSelectQuestions
)

val mockImageWordMatchTestDetail = TestDetail(
    id = "test-iwm",
    categoryId = "cat-2",
    title = "Room Objects",
    description = "Learn room vocabulary by matching words to objects",
    thumbnailUrl = null,
    difficulty = Difficulty.EASY,
    pointsReward = 50,
    timeLimitSeconds = 300,
    questions = mockImageWordMatchQuestions
)

// ============================================
// ACHIEVEMENT MOCKS
// ============================================

val mockAchievements = listOf(
    Achievement(
        id = "ach-1",
        code = "first_steps",
        name = "First Steps",
        description = "Complete your first test",
        iconUrl = null,
        category = AchievementCategory.LEARNING,
        rarity = Rarity.COMMON,
        isHidden = false,
        condition = null,
        pointsReward = 50
    ),
    Achievement(
        id = "ach-2",
        code = "streak_master",
        name = "Streak Master",
        description = "Maintain a 7-day learning streak",
        iconUrl = null,
        category = AchievementCategory.CONSISTENCY,
        rarity = Rarity.RARE,
        isHidden = false,
        condition = null,
        pointsReward = 100
    ),
    Achievement(
        id = "ach-3",
        code = "word_wizard",
        name = "Word Wizard",
        description = "Learn 100 words",
        iconUrl = null,
        category = AchievementCategory.LEARNING,
        rarity = Rarity.EPIC,
        isHidden = false,
        condition = null,
        pointsReward = 250
    )
)

val mockUserAchievements = listOf(
    UserAchievement(
        achievement = mockAchievements[0],
        earnedAt = "2024-01-10T10:00:00Z",
        progress = 1f,
        isEarned = true
    ),
    UserAchievement(
        achievement = mockAchievements[1],
        earnedAt = null,
        progress = 0.7f,
        isEarned = false
    ),
    UserAchievement(
        achievement = mockAchievements[2],
        earnedAt = null,
        progress = 0.3f,
        isEarned = false
    )
)

// ============================================
// STREAK MOCKS
// ============================================

val mockStreakData = StreakData(
    currentStreak = 7,
    longestStreak = 14,
    weeklyCalendar = listOf(
        DayStatus("2024-01-14", StreakDayStatus.COMPLETED, 50),
        DayStatus("2024-01-15", StreakDayStatus.TODAY_PENDING, 0)
    ),
    streakFreezesAvailable = 2,
    nextMilestone = 14,
    isAtRisk = false,
    lastActivityDate = "2024-01-14",
    recoveryChallengeAvailable = false
)

// ============================================
// TEST RESULT MOCKS
// ============================================

val mockSubmitTestResult = SubmitTestResult(
    score = 80,
    maxScore = 100,
    percentage = 80,
    stars = 2,
    pointsEarned = 40,
    isNewBestScore = true,
    newAchievements = listOf(mockAchievements[0]),
    levelUp = null
)

// ============================================
// SPEAKING TRAINER MOCKS (спека Part 2 §10.1)
// ============================================

/** Сырой список тем-библиотек: вторая с topicCount=0 — на экран попадает
 *  уже отфильтрованный список (см. [mockVisibleSpeakingLibraries]). */
val mockSpeakingLibraries = listOf(
    SpeakingLibrary(
        id = "lib-1",
        title = "Знакомство",
        description = "Базовые фразы для знакомства",
        coverUrl = null,
        topicCount = 3
    ),
    SpeakingLibrary(
        id = "lib-2",
        title = "Пустая тема",
        description = null,
        coverUrl = null,
        topicCount = 0
    )
)

/** Отфильтрованные (topicCount > 0) — то, что реально рендерит LibraryScreen. */
val mockVisibleSpeakingLibraries = mockSpeakingLibraries.filter { it.topicCount > 0 }

/** Топики: с субтитрами/просмотрен и без субтитров/не просмотрен + есть локальные записи. */
val mockSpeakingTopics = listOf(
    TopicUiModel(
        id = "topic-1",
        title = "Приветствие",
        durationSeconds = 95,
        hasSubtitles = true,
        isWatched = true,
        hasLocalRecordings = true
    ),
    TopicUiModel(
        id = "topic-2",
        title = "О себе",
        durationSeconds = 120,
        hasSubtitles = false,
        isWatched = false,
        hasLocalRecordings = false
    )
)

val mockSpeakingQuestions = listOf(
    SpeakingQuestion(id = "sq-1", text = "What is your name?", displayOrder = 1),
    SpeakingQuestion(id = "sq-2", text = "Where do you live?", displayOrder = 2),
    SpeakingQuestion(id = "sq-3", text = "What do you like to do?", displayOrder = 3)
)

/** Полная рубрика SpeakingGrade для REVIEWED-сабмишена. */
val mockSpeakingGrade = SpeakingGrade(
    grammar = 8,
    vocabulary = 7,
    pronunciation = 9,
    fluency = 6,
    total = 7.5,
    comment = "Хорошая работа! Обрати внимание на артикли.",
    reviewerName = "Teacher Anna",
    createdAt = "2026-07-30T12:00:00Z",
    updatedAt = "2026-07-30T12:00:00Z"
)

val mockSpeakingSubmissions = listOf(
    SpeakingSubmission(
        id = "sub-1",
        topicId = "topic-1",
        topicTitle = "Приветствие",
        audioUrl = "https://media.example.com/sub-1.m4a",
        durationSec = 28,
        status = "NEW",
        grade = null,
        createdAt = "2026-07-31T10:00:00Z"
    ),
    SpeakingSubmission(
        id = "sub-2",
        topicId = "topic-2",
        topicTitle = "О себе",
        audioUrl = "https://media.example.com/sub-2.m4a",
        durationSec = 30,
        status = "REVIEWED",
        grade = mockSpeakingGrade,
        createdAt = "2026-07-29T09:00:00Z"
    )
)

/** Две training-попытки (локальные записи, макс. 3 на топик). */
val mockTrainingRecordingMetas = listOf(
    RecordingMeta(
        filePath = "/recordings/topic-1_training_1.m4a",
        topicId = "topic-1",
        attemptNumber = 1,
        kind = RecordingKind.TRAINING,
        durationMs = 75_000,
        timerLimitSeconds = 80,
        createdAtEpochMs = 1_753_000_000_000
    ),
    RecordingMeta(
        filePath = "/recordings/topic-1_training_2.m4a",
        topicId = "topic-1",
        attemptNumber = 2,
        kind = RecordingKind.TRAINING,
        durationMs = 48_000,
        timerLimitSeconds = 50,
        createdAtEpochMs = 1_753_000_100_000
    )
)

/** Неотправленная practice-запись (offline retry, спека §6.4). */
val mockPendingUploads = listOf(
    RecordingMeta(
        filePath = "/recordings/topic-2_practice_0.m4a",
        topicId = "topic-2",
        attemptNumber = 0,
        kind = RecordingKind.PRACTICE,
        durationMs = 30_000,
        timerLimitSeconds = 30,
        createdAtEpochMs = 1_753_000_200_000,
        uploaded = false
    )
)

// ============================================
// API MOCKS
// ============================================

class MockFunnyEnglishApi {
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return if (email == "test@example.com" && password == "password123") {
            Result.success(
                AuthResponse(
                    token = "mock_token",
                    user = mockUser
                )
            )
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }
    
    suspend fun getCategories(): Result<List<Category>> {
        return Result.success(mockCategories)
    }
    
    suspend fun getTests(): Result<List<TestListItem>> {
        return Result.success(mockTestListItems)
    }
    
    suspend fun getTestDetail(testId: String): Result<TestDetail> {
        return Result.success(
            when (testId) {
                "test-iwm" -> mockImageWordMatchTestDetail
                else -> mockTestDetail
            }
        )
    }
    
    suspend fun submitTest(testId: String, answers: List<SubmitAnswer>): Result<SubmitTestResult> {
        return Result.success(mockSubmitTestResult)
    }
    
    suspend fun getUserAchievements(): Result<List<UserAchievement>> {
        return Result.success(mockUserAchievements)
    }
    
    suspend fun getStreak(): Result<StreakData> {
        return Result.success(mockStreakData)
    }
}

// ============================================
// REPOSITORY MOCKS
// ============================================

class MockTestRepository {
    fun getTests(): Flow<List<TestListItem>> = flowOf(mockTestListItems)
    fun getTestDetail(testId: String): Flow<TestDetail> = flowOf(
        when (testId) {
            "test-iwm" -> mockImageWordMatchTestDetail
            else -> mockTestDetail
        }
    )
    suspend fun submitTest(testId: String, answers: List<SubmitAnswer>): Result<SubmitTestResult> {
        return Result.success(
            SubmitTestResult(
                score = 100,
                maxScore = 100,
                percentage = 100,
                stars = 3,
                pointsEarned = 50,
                isNewBestScore = true,
                newAchievements = listOf(mockAchievements[0]),
                levelUp = null
            )
        )
    }
}

class MockUserRepository {
    fun getCurrentUser(): Flow<User> = flowOf(mockUser)
    fun getUserProfile(): Flow<UserProfile> = flowOf(mockUserProfile)
}

class MockAchievementRepository {
    fun getAchievements(): Flow<List<Achievement>> = flowOf(mockAchievements)
    fun getUserAchievements(): Flow<List<UserAchievement>> = flowOf(mockUserAchievements)
}
