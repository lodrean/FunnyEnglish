package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.recorder.MicPermissionState
import com.funnyenglish.app.viewmodel.RecorderUiState
import com.funnyenglish.app.viewmodel.TrainingState
import com.funnyenglish.app.viewmodel.TrainingViewModel
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes
import com.funnyenglish.designsystem.theme.SpeakingTextStyles

/**
 * Экран Training (спека Part 2 §2.5, §5.3; дизайн Playful Coach v1.1):
 * 3 попытки на топик (80/50/30), одна запись = ВСЕ вопросы (список на экране),
 * без удаления — только прослушивание, авто-✅, финальные CTA после 3-й попытки.
 *
 * Управление VoiceRecorder — здесь (remember + DisposableEffect, спека §8.1);
 * VM владеет таймером и состоянием.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    state: TrainingState,
    topicId: String,
    recorderState: com.funnyenglish.app.recorder.VoiceRecorderState,
    micPermission: MicPermissionState,
    onStartRecording: () -> Unit,       // экран: recorder.start(fileName)
    onStopRecording: () -> Unit,        // экран: recorder.stop()
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onGoToPractice: () -> Unit,
    onRestartAttempts: () -> Unit,
    onBackToLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.topicTitle.ifBlank { "Тренировка" },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
            )
        },
        modifier = modifier.testTag("training_screen")
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.error != null -> ErrorMessage(
                message = state.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding)
            )
            else -> TrainingContent(
                state = state,
                recorderState = recorderState,
                micPermission = micPermission,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onPlayRecording = onPlayRecording,
                onStopPlayback = onStopPlayback,
                onGoToPractice = onGoToPractice,
                onRestartAttempts = onRestartAttempts,
                onBackToLibrary = onBackToLibrary,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun TrainingContent(
    state: TrainingState,
    recorderState: com.funnyenglish.app.recorder.VoiceRecorderState,
    micPermission: MicPermissionState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onGoToPractice: () -> Unit,
    onRestartAttempts: () -> Unit,
    onBackToLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val isRecording = state.recorder is RecorderUiState.Recording
    val limit = TrainingViewModel.timerLimitFor(state.attemptNumber)
    val timerColor = when (state.attemptNumber) {
        1 -> speaking.timerLevel80
        2 -> speaking.timerLevel50
        else -> speaking.timerLevel30
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Level-chip + индикаторы попыток
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = timerColor.copy(alpha = 0.15f),
                    modifier = Modifier.testTag("level_chip")
                ) {
                    Text(
                        "Уровень ${state.attemptNumber} · $limit сек",
                        color = timerColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                repeat(TrainingViewModel.MAX_ATTEMPTS) { index ->
                    val done = index < state.attempts.size
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (done) speaking.success else speaking.surfaceVariant
                            )
                    )
                }
            }
        }

        // Весь список вопросов — отвечаем на все одной записью
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("training_questions_list")
            ) {
                state.questions.forEachIndexed { index, question ->
                    Card(
                        shape = SpeakingShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = speaking.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_item_$index")
                    ) {
                        Text(
                            question.text,
                            style = SpeakingTextStyles.QuestionText,
                            color = speaking.text,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Таймер + rec-кнопка (скрываются после 3-й попытки)
        if (!state.isFinished) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (micPermission == MicPermissionState.Denied ||
                        micPermission == MicPermissionState.PermanentlyDenied
                    ) {
                        MicPermissionRationale(
                            permanentlyDenied = micPermission == MicPermissionState.PermanentlyDenied,
                            onOpenSettings = onOpenSettings
                        )
                    }

                    if (isRecording) {
                        Text(
                            formatTimer(state.remainingSeconds),
                            style = SpeakingTextStyles.TimerDisplay,
                            color = timerColor,
                            modifier = Modifier.testTag("training_timer")
                        )
                        // Прогресс-кольцо (guard: limit > 0 по построению)
                        LinearProgressIndicator(
                            progress = {
                                (state.remainingSeconds / limit.toFloat()).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            color = timerColor,
                            trackColor = speaking.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RecordButton(
                            isRecording = true,
                            enabled = true,
                            onClick = onStopRecording,
                            testTag = "stop_button",
                            label = "Стоп"
                        )
                    } else {
                        Text(
                            "Ответь на все вопросы одним голосовым сообщением",
                            style = MaterialTheme.typography.bodyMedium,
                            color = speaking.textMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RecordButton(
                            isRecording = false,
                            enabled = micPermission == MicPermissionState.Granted,
                            onClick = onStartRecording,
                            testTag = "record_button",
                            label = "Запись"
                        )
                        if (state.recorder is RecorderUiState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (state.recorder as RecorderUiState.Error).message,
                                color = speaking.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Список попыток: только прослушивание, ✅ автоматически, удаления нет
        if (state.attempts.isNotEmpty()) {
            item {
                Text(
                    "Твои попытки",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.text
                )
            }
            itemsIndexed(state.attempts, key = { _, a -> a.filePath }) { index, attempt ->
                AttemptCard(
                    index = index,
                    durationMs = attempt.durationMs,
                    isPlaying = state.playingRecordingPath == attempt.filePath,
                    onPlay = {
                        if (state.playingRecordingPath == attempt.filePath) onStopPlayback()
                        else onPlayRecording(attempt.filePath)
                    }
                )
            }
        }

        // Финальный блок после 3-й попытки
        if (state.isFinished) {
            item {
                FinalCtaBlock(
                    onGoToPractice = onGoToPractice,
                    onBackToLibrary = onBackToLibrary,
                    onRestartAttempts = onRestartAttempts
                )
            }
        }

        // Privacy-note
        item {
            Text(
                "Записи хранятся только на твоём устройстве",
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecordButton(
    isRecording: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    label: String
) {
    val speaking = LocalSpeakingColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Squircle 22dp с жёсткой «оттопыренной» тенью 0 4px 0 recordShadow (tokens.json)
        Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(72.dp)
                .clip(SpeakingShapes.Recorder)
                .background(if (enabled) speaking.recordShadow else speaking.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxSize()
                    .clip(SpeakingShapes.Recorder)
                    .background(if (enabled) speaking.record else speaking.outline)
                    .clickable(enabled = enabled, onClick = onClick)
                    .testTag(testTag),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = label,
                    tint = speaking.onRecord,   // тёмный на record (WCAG AA)
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) speaking.text else speaking.textMuted
        )
    }
}

@Composable
private fun AttemptCard(
    index: Int,
    durationMs: Long,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recording_item_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPlay,
                modifier = Modifier.testTag("play_recording_$index")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Стоп" else "Прослушать",
                    tint = speaking.waveformPlayback
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Попытка ${index + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = speaking.text
                )
                Text(
                    formatTimer((durationMs / 1000).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = speaking.textMuted
                )
            }
            // ✅ «принята» — автоматически (дизайн v1.1)
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Принята",
                tint = speaking.success,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("attempt_check_$index")
            )
        }
    }
}

@Composable
private fun FinalCtaBlock(
    onGoToPractice: () -> Unit,
    onBackToLibrary: () -> Unit,
    onRestartAttempts: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("final_cta")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Все 3 попытки готовы! 🎉",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = speaking.text
            )
            Button(
                onClick = onGoToPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_go_practice"),
                colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
            ) {
                Text("Перейти к практике", color = speaking.onRecord)
            }
            OutlinedButton(
                onClick = onBackToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_back_library")
            ) {
                Text("Вернуться в библиотеку")
            }
            TextButton(
                onClick = onRestartAttempts,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("final_restart")
            ) {
                Text("Начать заново с попытки 1")
            }
        }
    }
}

@Composable
private fun MicPermissionRationale(
    permanentlyDenied: Boolean,
    onOpenSettings: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("mic_permission_rationale")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Для записи голоса нужен доступ к микрофону",
                style = MaterialTheme.typography.bodyMedium,
                color = speaking.text
            )
            if (permanentlyDenied) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenSettings) {
                    Text("Открыть настройки")
                }
            }
        }
    }
}

internal fun formatTimer(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

// ==================== Route (связка VM + VoiceRecorder + permission + lifecycle) ====================

/**
 * Обёртка Training-экрана (спека §4.4, §5, §8.1):
 * - VoiceRecorder — remember + DisposableEffect (НЕ в Koin);
 * - VoiceRecorder.state → действия VM (started/stopped/error);
 * - автостоп по таймеру (remainingSeconds == 0);
 * - прерывание ON_PAUSE → stop (НЕ cancel) — попытка засчитывается;
 * - разрешение RECORD_AUDIO через rememberMicrophonePermissionState.
 */
@Composable
fun TrainingRoute(
    topicId: String,
    onNavigateToPractice: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: com.funnyenglish.app.viewmodel.TrainingViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()
    val currentState by rememberUpdatedState(state)

    val recorder = remember { com.funnyenglish.app.recorder.VoiceRecorder() }
    val recorderState by recorder.state.collectAsState()
    val currentRecorderState by rememberUpdatedState(recorderState)
    val recordingStore = org.koin.compose.koinInject<com.funnyenglish.app.storage.RecordingStore>()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    val micPermission = com.funnyenglish.app.recorder.rememberMicrophonePermissionState { granted ->
        vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnPermissionResult(granted))
    }
    LaunchedEffect(micPermission) {
        vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnPermissionState(micPermission))
    }

    LaunchedEffect(topicId) {
        vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnLoad(topicId))
    }

    com.funnyenglish.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.funnyenglish.app.viewmodel.TrainingEvent.NavigateToLibrary -> onNavigateToLibrary()
            is com.funnyenglish.app.viewmodel.TrainingEvent.NavigateToPractice -> onNavigateToPractice()
            is com.funnyenglish.app.viewmodel.TrainingEvent.NavigateBack -> onNavigateBack()
            is com.funnyenglish.app.viewmodel.TrainingEvent.ShowMessage ->
                snackbarHostState.showSnackbar(event.text)
        }
    }

    // VoiceRecorder.state → VM
    LaunchedEffect(recorderState) {
        when (val rs = recorderState) {
            is com.funnyenglish.app.recorder.VoiceRecorderState.Recording ->
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnRecorderStarted)
            is com.funnyenglish.app.recorder.VoiceRecorderState.Stopped ->
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnRecorderStopped(rs.filePath))
            is com.funnyenglish.app.recorder.VoiceRecorderState.Error ->
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnRecorderError(rs.message))
            else -> {}
        }
    }

    // Автостоп по таймеру
    LaunchedEffect(state.remainingSeconds) {
        if (state.recorder is com.funnyenglish.app.viewmodel.RecorderUiState.Recording &&
            state.remainingSeconds <= 0
        ) {
            recorder.stop()
        }
    }

    // Прерывание (сворачивание) → stop, попытка засчитывается (спека §4.4)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE &&
                currentRecorderState is com.funnyenglish.app.recorder.VoiceRecorderState.Recording
            ) {
                recorder.stop()
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnInterruption)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TrainingScreen(
            state = state,
            topicId = topicId,
            recorderState = recorderState,
            micPermission = micPermission,
            onStartRecording = {
                if (micPermission == com.funnyenglish.app.recorder.MicPermissionState.Granted) {
                    vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnStartRecording)
                    val fileName = recordingStore.fileNameFor(
                        topicId = topicId,
                        kind = com.funnyenglish.app.storage.RecordingKind.TRAINING,
                        attemptNumber = currentState.attemptNumber,
                        epochMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    )
                    recorder.start(fileName)
                }
            },
            onStopRecording = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnStopRecording)
                recorder.stop()
            },
            onPlayRecording = { path ->
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnPlayRecording(path))
            },
            onStopPlayback = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnStopPlayback)
            },
            onGoToPractice = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnGoToPractice)
            },
            onRestartAttempts = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnRestartAttempts)
            },
            onBackToLibrary = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnBackToLibrary)
            },
            onOpenSettings = { /* «Открыть настройки» — android-обработка в T11 */ },
            onRetry = {
                vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnLoad(topicId))
            },
            onBack = { vm.onAction(com.funnyenglish.app.viewmodel.TrainingAction.OnBack) }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
