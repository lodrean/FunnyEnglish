package com.sotospeak.app.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.CheckPopAppear
import com.sotospeak.app.components.RecIndicator
import com.sotospeak.app.components.SpeakingRecordButton
import com.sotospeak.app.components.SpeakingTimerRing
import com.sotospeak.app.screens.MySubmissionsScreen
import com.sotospeak.app.screens.ProfileScreen
import com.sotospeak.app.screens.QuestionsScreen
import com.sotospeak.app.storage.RecordingKind
import com.sotospeak.app.storage.RecordingMeta
import com.sotospeak.app.viewmodel.AppThemeMode
import com.sotospeak.app.viewmodel.MySubmissionsState
import com.sotospeak.app.viewmodel.ProfileState
import com.sotospeak.app.viewmodel.QuestionsState
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.FunnyThemePreview
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.shared.model.SpeakingGrade
import com.sotospeak.shared.model.SpeakingQuestion
import com.sotospeak.shared.model.SpeakingSubmission
import com.sotospeak.shared.model.User
import com.sotospeak.shared.model.UserProfile

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLightPreview() {
    FunnyThemePreview(darkTheme = false) {
        ProfileScreen(
            state = ProfileState(
                userProfile = UserProfile(
                    user = User(
                        id = "1",
                        email = "anna@smirnova.ru",
                        displayName = "Анна Смирнова",
                        level = 1,
                        totalPoints = 0,
                        currentStreak = 0,
                        role = "USER",
                        createdAt = "2026-01-01T00:00:00Z"
                    )
                )
            ),
            isGuest = false,
            submissionsCount = 3,
            topicsCompleted = 2,
            themeMode = AppThemeMode.SYSTEM,
            onThemeSelected = {},
            onLoad = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenDarkPreview() {
    FunnyThemePreview(darkTheme = true) {
        ProfileScreen(
            state = ProfileState(
                userProfile = UserProfile(
                    user = User(
                        id = "1",
                        email = "anna@smirnova.ru",
                        displayName = "Анна Смирнова",
                        level = 1,
                        totalPoints = 0,
                        currentStreak = 0,
                        role = "USER",
                        createdAt = "2026-01-01T00:00:00Z"
                    )
                )
            ),
            isGuest = false,
            submissionsCount = 3,
            topicsCompleted = 2,
            themeMode = AppThemeMode.DARK,
            onThemeSelected = {},
            onLoad = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestProfileScreenPreview() {
    FunnyThemePreview(darkTheme = false) {
        ProfileScreen(
            state = ProfileState(),
            isGuest = true,
            onThemeSelected = {},
            onLoad = {},
            onLogout = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MySubmissionsScreenLightPreview() {
    FunnyThemePreview(darkTheme = false) {
        MySubmissionsScreen(
            state = MySubmissionsState(
                submissions = listOf(
                    SpeakingSubmission(
                        id = "1",
                        topicId = "t1",
                        topicTitle = "At the airport",
                        audioUrl = "",
                        durationSec = 30,
                        status = "NEW",
                        createdAt = "2026-08-01T10:00:00Z"
                    ),
                    SpeakingSubmission(
                        id = "2",
                        topicId = "t2",
                        topicTitle = "Hotel check-in",
                        audioUrl = "",
                        durationSec = 28,
                        status = "REVIEWED",
                        createdAt = "2026-08-02T12:00:00Z",
                        grade = SpeakingGrade(
                            grammar = 7,
                            vocabulary = 6,
                            pronunciation = 8,
                            fluency = 7,
                            total = 7.0,
                            comment = "Хорошая работа!",
                            reviewerName = "Иван Петров"
                        )
                    )
                ),
                pendingUploads = listOf(
                    RecordingMeta(
                        filePath = "/tmp/recording.m4a",
                        topicId = "t3",
                        attemptNumber = 0,
                        kind = RecordingKind.PRACTICE,
                        durationMs = 12000,
                        timerLimitSeconds = 30,
                        createdAtEpochMs = 0
                    )
                )
            ),
            onRefresh = {},
            onRetryPending = {},
            onPlayAudio = {},
            onStopAudio = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MySubmissionsScreenDarkPreview() {
    FunnyThemePreview(darkTheme = true) {
        MySubmissionsScreen(
            state = MySubmissionsState(
                submissions = listOf(
                    SpeakingSubmission(
                        id = "1",
                        topicId = "t1",
                        topicTitle = "At the airport",
                        audioUrl = "",
                        durationSec = 30,
                        status = "NEW",
                        createdAt = "2026-08-01T10:00:00Z"
                    ),
                    SpeakingSubmission(
                        id = "2",
                        topicId = "t2",
                        topicTitle = "Hotel check-in",
                        audioUrl = "",
                        durationSec = 28,
                        status = "REVIEWED",
                        createdAt = "2026-08-02T12:00:00Z",
                        grade = SpeakingGrade(
                            grammar = 7,
                            vocabulary = 6,
                            pronunciation = 8,
                            fluency = 7,
                            total = 7.0,
                            comment = "Хорошая работа!",
                            reviewerName = "Иван Петров"
                        )
                    )
                )
            ),
            onRefresh = {},
            onRetryPending = {},
            onPlayAudio = {},
            onStopAudio = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestionsScreenLightPreview() {
    FunnyThemePreview(darkTheme = false) {
        QuestionsScreen(
            state = QuestionsState(
                topicTitle = "At the airport",
                questions = listOf(
                    SpeakingQuestion("1", "Where do you usually fly to on holidays?", 1),
                    SpeakingQuestion("2", "Do you prefer window or aisle seats?", 2)
                ),
                isGuest = false,
                hasSubmitted = false
            ),
            onStartTraining = {},
            onStartPractice = {},
            onLoginClick = {},
            onRegisterClick = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestionsScreenSubmittedPreview() {
    FunnyThemePreview(darkTheme = false) {
        QuestionsScreen(
            state = QuestionsState(
                topicTitle = "At the airport",
                questions = listOf(
                    SpeakingQuestion("1", "Where do you usually fly to on holidays?", 1)
                ),
                isGuest = false,
                hasSubmitted = true
            ),
            onStartTraining = {},
            onStartPractice = {},
            onLoginClick = {},
            onRegisterClick = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestionsScreenGuestPreview() {
    FunnyThemePreview(darkTheme = false) {
        QuestionsScreen(
            state = QuestionsState(
                topicTitle = "At the airport",
                questions = listOf(
                    SpeakingQuestion("1", "Where do you usually fly to on holidays?", 1)
                ),
                isGuest = true,
                hasSubmitted = false
            ),
            onStartTraining = {},
            onStartPractice = {},
            onLoginClick = {},
            onRegisterClick = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeakingRecordButtonPreview() {
    FunnyThemePreview {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
            SpeakingRecordButton(isRecording = false, enabled = true, onClick = {}, testTag = "record")
            SpeakingRecordButton(isRecording = true, enabled = true, onClick = {}, testTag = "stop")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeakingTimerRingPreview() {
    FunnyThemePreview {
        SpeakingTimerRing(
            remainingSeconds = 25,
            totalSeconds = 30,
            arcColor = LocalSpeakingColors.current.timerLevel30,
            timeText = "0:25",
            caption = "на все ответы"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecIndicatorPreview() {
    FunnyThemePreview {
        RecIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckPopAppearPreview() {
    FunnyThemePreview {
        CheckPopAppear {
            Icon(
                imageVector = SpeakingIcons.CheckCircle,
                contentDescription = null,
                tint = LocalSpeakingColors.current.success,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
