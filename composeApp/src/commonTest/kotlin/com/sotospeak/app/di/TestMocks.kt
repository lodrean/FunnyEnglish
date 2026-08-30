package com.sotospeak.app.di

import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.viewmodel.TopicUiModel
import com.sotospeak.shared.contracts.*

/**
 * Мок-реализации для тестирования.
 * Используются shared контракты из com.sotospeak.shared.contracts
 */

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
        questionCount = 5,
        hasSubtitles = true,
        isWatched = true,
        hasLocalRecordings = true
    ),
    TopicUiModel(
        id = "topic-2",
        title = "О себе",
        durationSeconds = 120,
        questionCount = 3,
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
