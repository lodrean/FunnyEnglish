package com.sotospeak.app.localization

import androidx.compose.runtime.staticCompositionLocalOf
import com.sotospeak.app.viewmodel.AppLanguage

object Strings {
    fun get(language: AppLanguage): AppStrings = when (language) {
        AppLanguage.RU -> RussianStrings
        AppLanguage.EN -> EnglishStrings
    }
}

interface AppStrings {
    val hello: String
    val continueLeaning: String
    val categories: String
    val recentTests: String
    val viewAll: String
    val settings: String
    val profile: String
    val leaderboard: String
    val achievements: String
    val logout: String
    val login: String
    val register: String
    val email: String
    val password: String
    val forgotPassword: String
    val notifications: String
    val sound: String
    val language: String
    val theme: String
    val light: String
    val dark: String
    val system: String
    val questionOf: String  // "Вопрос X из Y"
    val complete: String    // "% завершено"
    val finishTest: String
    val checkAnswer: String
    val tryAgain: String
    val backToHome: String
    val excellent: String
    val good: String
    val notBad: String
    val keepTrying: String
    val yourResult: String
    val newRecord: String
    val newLevel: String
    val newAchievements: String

    // ==================== Speaking-экраны (Library → MySubmissions) ====================

    // LibraryScreen
    val libraryTitle: String
    val librarySubtitle: String
    val libraryEmptyTitle: String
    val libraryEmptySubtitle: String
    val ctaRefresh: String
    val themeStatusNew: String
    val topicsCompletedTemplate: String  // "{0} ПРОЙДЕНО"
    fun topicsCompleted(count: Int): String =
        topicsCompletedTemplate.replace("{0}", count.toString())
    /** Плюрализация: «1 топик», «3 топика», «6 топиков» / "1 topic", "3 topics". */
    fun topicsCount(count: Int): String

    // TopicsScreen
    val topicsTitle: String
    val topicsSubtitleTemplate: String  // "{0} · выбери и начни говорить"
    fun topicsSubtitle(topicCount: Int): String =
        topicsSubtitleTemplate.replace("{0}", topicsCount(topicCount))
    val topicsEmptyTitle: String
    val topicsEmptySubtitle: String
    val topicStatusDone: String
    val topicStatusNew: String
    val videoLabel: String  // «видео m:ss»
    /** Плюрализация: «1 вопрос», «3 вопроса», «5 вопросов» / "1 question", "5 questions". */
    fun questionsCount(count: Int): String

    // QuestionsScreen
    val questionsTitle: String
    val trainingModeButton: String
    val gateTitle: String
    val gateText: String
    val registerCta: String
    val submittedButton: String
    val practiceModeButton: String
    val questionOfUpperTemplate: String  // "ВОПРОС {0} ИЗ {1}"
    fun questionOfUpper(current: Int, total: Int): String =
        questionOfUpperTemplate
            .replace("{0}", current.toString())
            .replace("{1}", total.toString())

    // VideoScreen
    val subtitlesOn: String
    val subtitlesOff: String
    val videoLoadError: String
    val retry: String
    val toQuestions: String
    val goToQuestions: String
    val videoHint: String
    val watchVideoDesc: String
    val startOver: String
    val pauseDesc: String
    val resumeDesc: String
    val exitFullscreenDesc: String
    val enterFullscreenDesc: String

    // TrainingScreen
    val levelChipTemplate: String  // "Уровень {0} · {1} сек"
    fun levelChip(attempt: Int, limitSec: Int): String =
        levelChipTemplate
            .replace("{0}", attempt.toString())
            .replace("{1}", limitSec.toString())
    val captionAttemptLimit: String
    val attemptHintTemplate: String  // "Попытка {0} · ответь на все вопросы одной записью"
    fun attemptHint(attempt: Int): String =
        attemptHintTemplate.replace("{0}", attempt.toString())
    val attemptsTitleTemplate: String  // "Попытки · {0} из {1}"
    fun attemptsTitle(done: Int, total: Int): String =
        attemptsTitleTemplate
            .replace("{0}", done.toString())
            .replace("{1}", total.toString())
    val attemptNumberTemplate: String  // "Попытка {0}"
    fun attemptNumber(attempt: Int): String =
        attemptNumberTemplate.replace("{0}", attempt.toString())
    val attemptAcceptedDesc: String
    val playbackStopDesc: String
    val playbackListenDesc: String
    val allAttemptsDone: String
    val goToPractice: String
    val backToLibrary: String
    val restartFromAttempt1: String
    val micPermissionTitle: String
    val openSettings: String
    val privacyNote: String
    val startRecordingDesc: String
    val stopRecordingDesc: String

    // PracticeScreen
    val backConfirmTitle: String
    val backConfirmRecording: String
    val backConfirmUploading: String
    val backConfirmLeave: String
    val backConfirmStay: String
    val practiceChipMain: String
    val practiceChipOneRec: String
    val practiceAutoSendNote: String
    val uploadErrorText: String
    val retryUpload: String
    val micPermissionShort: String
    val captionAllAnswers: String
    val readyHint: String
    val finishAndSend: String
    val uploadingTitle: String
    val sentTitle: String
    val sentStatus: String
    val sentNote: String

    // MySubmissionsScreen
    val submissionsTitle: String
    val submissionsSubtitle: String
    val submissionsEmptyTitle: String
    val submissionsEmptySubtitle: String
    val notSentSection: String
    val pendingUploadText: String
    val submissionsExplainer: String
    val teacherGrade: String
    val gradeGrammar: String
    val gradeVocabulary: String
    val gradePronunciation: String
    val gradeFluency: String
    val reviewedByTemplate: String  // "Проверил: {0}"
    fun reviewedBy(name: String): String =
        reviewedByTemplate.replace("{0}", name)
}

object RussianStrings : AppStrings {
    override val hello = "Привет"
    override val continueLeaning = "Продолжить обучение"
    override val categories = "Категории"
    override val recentTests = "Недавние тесты"
    override val viewAll = "Все"
    override val settings = "Настройки"
    override val profile = "Профиль"
    override val leaderboard = "Лидеры"
    override val achievements = "Достижения"
    override val logout = "Выйти"
    override val login = "Войти"
    override val register = "Регистрация"
    override val email = "Email"
    override val password = "Пароль"
    override val forgotPassword = "Забыли пароль?"
    override val notifications = "Уведомления"
    override val sound = "Звук"
    override val language = "Язык"
    override val theme = "Тема"
    override val light = "Светлая"
    override val dark = "Тёмная"
    override val system = "Системная"
    override val questionOf = "Вопрос %d из %d"
    override val complete = "%d%% завершено"
    override val finishTest = "Завершить тест"
    override val checkAnswer = "Проверить ответ"
    override val tryAgain = "Попробовать снова"
    override val backToHome = "На главную"
    override val excellent = "Отлично!"
    override val good = "Хорошо!"
    override val notBad = "Неплохо!"
    override val keepTrying = "Попробуй ещё!"
    override val yourResult = "Ваш результат"
    override val newRecord = "Рекорд!"
    override val newLevel = "Новый уровень!"
    override val newAchievements = "Новые достижения"

    // LibraryScreen
    override val libraryTitle = "Библиотека тем"
    override val librarySubtitle = "Выбери тему и начни говорить"
    override val libraryEmptyTitle = "Пока нет доступных тем"
    override val libraryEmptySubtitle = "Загляни позже — темы появятся скоро"
    override val ctaRefresh = "Обновить"
    override val themeStatusNew = "НОВАЯ"
    override val topicsCompletedTemplate = "{0} ПРОЙДЕНО"
    override fun topicsCount(count: Int): String {
        val mod100 = count % 100
        val mod10 = count % 10
        val word = when {
            mod100 in 11..14 -> "топиков"
            mod10 == 1 -> "топик"
            mod10 in 2..4 -> "топика"
            else -> "топиков"
        }
        return "$count $word"
    }

    // TopicsScreen
    override val topicsTitle = "Топики"
    override val topicsSubtitleTemplate = "{0} · выбери и начни говорить"
    override val topicsEmptyTitle = "В этой теме пока нет топиков"
    override val topicsEmptySubtitle = "Загляни позже — топики появятся скоро"
    override val topicStatusDone = "пройден"
    override val topicStatusNew = "новый"
    override val videoLabel = "видео"
    override fun questionsCount(count: Int): String {
        val mod100 = count % 100
        val mod10 = count % 10
        val word = when {
            mod100 in 11..14 -> "вопросов"
            mod10 == 1 -> "вопрос"
            mod10 in 2..4 -> "вопроса"
            else -> "вопросов"
        }
        return "$count $word"
    }

    // QuestionsScreen
    override val questionsTitle = "Вопросы"
    override val trainingModeButton = "Тренировка · 3 попытки"
    override val gateTitle = "Ты почти у цели!"
    override val gateText = "Отправка записи учителю доступна после регистрации"
    override val registerCta = "Зарегистрироваться"
    override val submittedButton = "Отправлено · мои записи"
    override val practiceModeButton = "Практика · 30 сек"
    override val questionOfUpperTemplate = "ВОПРОС {0} ИЗ {1}"

    // VideoScreen
    override val subtitlesOn = "С субтитрами"
    override val subtitlesOff = "Без субтитров"
    override val videoLoadError = "Не удалось загрузить видео"
    override val retry = "Повторить"
    override val toQuestions = "К вопросам"
    override val goToQuestions = "Перейти к вопросам"
    override val videoHint = "Смотреть всё видео необязательно — к вопросам можно перейти в любой момент"
    override val watchVideoDesc = "Смотреть видео"
    override val startOver = "Начать заново"
    override val pauseDesc = "Пауза"
    override val resumeDesc = "Продолжить"
    override val exitFullscreenDesc = "Свернуть видео"
    override val enterFullscreenDesc = "На весь экран"

    // TrainingScreen
    override val levelChipTemplate = "Уровень {0} · {1} сек"
    override val captionAttemptLimit = "лимит попытки"
    override val attemptHintTemplate = "Попытка {0} · ответь на все вопросы одной записью"
    override val attemptsTitleTemplate = "Попытки · {0} из {1}"
    override val attemptNumberTemplate = "Попытка {0}"
    override val attemptAcceptedDesc = "Принята"
    override val playbackStopDesc = "Стоп"
    override val playbackListenDesc = "Прослушать"
    override val allAttemptsDone = "Все 3 попытки готовы! 🎉"
    override val goToPractice = "Перейти к практике"
    override val backToLibrary = "Вернуться в библиотеку"
    override val restartFromAttempt1 = "Начать заново с попытки 1"
    override val micPermissionTitle = "Для записи голоса нужен доступ к микрофону"
    override val openSettings = "Открыть настройки"
    override val privacyNote = "Записи хранятся только на твоём устройстве"
    override val startRecordingDesc = "Начать запись"
    override val stopRecordingDesc = "Остановить запись"

    // PracticeScreen
    override val backConfirmTitle = "Прервать запись?"
    override val backConfirmRecording = "Запись будет потеряна."
    override val backConfirmUploading = "Отправка прервётся — запись останется на устройстве."
    override val backConfirmLeave = "Выйти"
    override val backConfirmStay = "Остаться"
    override val practiceChipMain = "Контрольная · 30 сек"
    override val practiceChipOneRec = "1 ЗАПИСЬ НА ВСЕ ВОПРОСЫ"
    override val practiceAutoSendNote = "В отличие от Training, эта запись уйдёт учителю автоматически сразу после остановки таймера — изменить её нельзя"
    override val uploadErrorText = "Не удалось отправить. Запись сохранена на устройстве."
    override val retryUpload = "Повторить отправку"
    override val micPermissionShort = "Для записи нужен доступ к микрофону"
    override val captionAllAnswers = "на все ответы"
    override val readyHint = "Ответь на все вопросы подряд одной записью"
    override val finishAndSend = "Закончить и отправить"
    override val uploadingTitle = "Отправка учителю…"
    override val sentTitle = "Запись отправлена!"
    override val sentStatus = "статус NEW · ждёт проверки"
    override val sentNote = "Оценка и комментарий появятся в «Отправки»"

    // MySubmissionsScreen
    override val submissionsTitle = "Отправки"
    override val submissionsSubtitle = "Записи, отправленные учителю"
    override val submissionsEmptyTitle = "У вас пока нет отправленных записей"
    override val submissionsEmptySubtitle = "Пройдите практику в любом топике библиотеки"
    override val notSentSection = "Не отправлено"
    override val pendingUploadText = "Запись ждёт отправки"
    override val submissionsExplainer = "Повторная отправка по топику запрещена — после REVIEWED топик можно только переиграть в Training"
    override val teacherGrade = "Оценка учителя"
    override val gradeGrammar = "Грамматика"
    override val gradeVocabulary = "Словарный запас"
    override val gradePronunciation = "Произношение"
    override val gradeFluency = "Беглость"
    override val reviewedByTemplate = "Проверил: {0}"
}

object EnglishStrings : AppStrings {
    override val hello = "Hello"
    override val continueLeaning = "Continue Learning"
    override val categories = "Categories"
    override val recentTests = "Recent Tests"
    override val viewAll = "View All"
    override val settings = "Settings"
    override val profile = "Profile"
    override val leaderboard = "Leaderboard"
    override val achievements = "Achievements"
    override val logout = "Log Out"
    override val login = "Sign In"
    override val register = "Sign Up"
    override val email = "Email"
    override val password = "Password"
    override val forgotPassword = "Forgot Password?"
    override val notifications = "Notifications"
    override val sound = "Sound"
    override val language = "Language"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System"
    override val questionOf = "Question %d of %d"
    override val complete = "%d%% complete"
    override val finishTest = "Finish Test"
    override val checkAnswer = "Check Answer"
    override val tryAgain = "Try Again"
    override val backToHome = "Back to Home"
    override val excellent = "Excellent!"
    override val good = "Good!"
    override val notBad = "Not Bad!"
    override val keepTrying = "Keep Trying!"
    override val yourResult = "Your Result"
    override val newRecord = "New Record!"
    override val newLevel = "Level Up!"
    override val newAchievements = "New Achievements"

    // LibraryScreen
    override val libraryTitle = "Topic Library"
    override val librarySubtitle = "Pick a topic and start speaking"
    override val libraryEmptyTitle = "No topics available yet"
    override val libraryEmptySubtitle = "Check back later — new topics are coming soon"
    override val ctaRefresh = "Refresh"
    override val themeStatusNew = "NEW"
    override val topicsCompletedTemplate = "{0} DONE"
    override fun topicsCount(count: Int): String =
        if (count == 1) "1 topic" else "$count topics"

    // TopicsScreen
    override val topicsTitle = "Topics"
    override val topicsSubtitleTemplate = "{0} · pick one and start speaking"
    override val topicsEmptyTitle = "No topics in this theme yet"
    override val topicsEmptySubtitle = "Check back later — topics are coming soon"
    override val topicStatusDone = "done"
    override val topicStatusNew = "new"
    override val videoLabel = "video"
    override fun questionsCount(count: Int): String =
        if (count == 1) "1 question" else "$count questions"

    // QuestionsScreen
    override val questionsTitle = "Questions"
    override val trainingModeButton = "Training · 3 attempts"
    override val gateTitle = "You're almost there!"
    override val gateText = "Sending your recording to a teacher is available after sign-up"
    override val registerCta = "Sign Up"
    override val submittedButton = "Submitted · my recordings"
    override val practiceModeButton = "Practice · 30 sec"
    override val questionOfUpperTemplate = "QUESTION {0} OF {1}"

    // VideoScreen
    override val subtitlesOn = "With subtitles"
    override val subtitlesOff = "Without subtitles"
    override val videoLoadError = "Failed to load the video"
    override val retry = "Retry"
    override val toQuestions = "To questions"
    override val goToQuestions = "Go to questions"
    override val videoHint = "You don't have to watch the whole video — you can jump to the questions at any time"
    override val watchVideoDesc = "Watch video"
    override val startOver = "Start over"
    override val pauseDesc = "Pause"
    override val resumeDesc = "Resume"
    override val exitFullscreenDesc = "Exit fullscreen"
    override val enterFullscreenDesc = "Fullscreen"

    // TrainingScreen
    override val levelChipTemplate = "Level {0} · {1} sec"
    override val captionAttemptLimit = "attempt limit"
    override val attemptHintTemplate = "Attempt {0} · answer all questions in one recording"
    override val attemptsTitleTemplate = "Attempts · {0} of {1}"
    override val attemptNumberTemplate = "Attempt {0}"
    override val attemptAcceptedDesc = "Accepted"
    override val playbackStopDesc = "Stop"
    override val playbackListenDesc = "Listen"
    override val allAttemptsDone = "All 3 attempts are done! 🎉"
    override val goToPractice = "Go to practice"
    override val backToLibrary = "Back to library"
    override val restartFromAttempt1 = "Start over from attempt 1"
    override val micPermissionTitle = "Microphone access is needed to record your voice"
    override val openSettings = "Open settings"
    override val privacyNote = "Recordings are stored only on your device"
    override val startRecordingDesc = "Start recording"
    override val stopRecordingDesc = "Stop recording"

    // PracticeScreen
    override val backConfirmTitle = "Interrupt the recording?"
    override val backConfirmRecording = "The recording will be lost."
    override val backConfirmUploading = "The upload will be interrupted — the recording will stay on your device."
    override val backConfirmLeave = "Leave"
    override val backConfirmStay = "Stay"
    override val practiceChipMain = "Final check · 30 sec"
    override val practiceChipOneRec = "1 RECORDING FOR ALL QUESTIONS"
    override val practiceAutoSendNote = "Unlike Training, this recording will be sent to the teacher automatically as soon as the timer stops — it can't be changed"
    override val uploadErrorText = "Failed to send. The recording is saved on your device."
    override val retryUpload = "Retry upload"
    override val micPermissionShort = "Microphone access is needed to record"
    override val captionAllAnswers = "for all answers"
    override val readyHint = "Answer all questions in a row in one recording"
    override val finishAndSend = "Finish and send"
    override val uploadingTitle = "Sending to the teacher…"
    override val sentTitle = "Recording sent!"
    override val sentStatus = "status NEW · awaiting review"
    override val sentNote = "Your grade and feedback will appear in «Submissions»"

    // MySubmissionsScreen
    override val submissionsTitle = "Submissions"
    override val submissionsSubtitle = "Recordings sent to the teacher"
    override val submissionsEmptyTitle = "You have no submitted recordings yet"
    override val submissionsEmptySubtitle = "Complete a practice in any library topic"
    override val notSentSection = "Not sent"
    override val pendingUploadText = "Recording is waiting to be sent"
    override val submissionsExplainer = "Re-submitting for a topic is not allowed — after REVIEWED the topic can only be replayed in Training"
    override val teacherGrade = "Teacher's grade"
    override val gradeGrammar = "Grammar"
    override val gradeVocabulary = "Vocabulary"
    override val gradePronunciation = "Pronunciation"
    override val gradeFluency = "Fluency"
    override val reviewedByTemplate = "Reviewed by: {0}"
}

/** Текущий язык UI-строк; по умолчанию RU (поведение не меняется). */
val LocalAppStrings = staticCompositionLocalOf<AppStrings> { RussianStrings }
