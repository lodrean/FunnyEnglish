package com.sotospeak.app.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.sotospeak.app.components.CheckPopAppear
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.components.PlaybackWaveform
import com.sotospeak.app.components.RecIndicator
import com.sotospeak.app.components.RecordingWaveform
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.components.SpeakingRecordButton
import com.sotospeak.app.components.SpeakingTimerRing
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.viewmodel.RecorderUiState
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.app.viewmodel.TrainingViewModel
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingElevation
import com.sotospeak.designsystem.theme.SpeakingMotion
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.designsystem.theme.SpeakingTextStyles

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
    recorderState: com.sotospeak.app.recorder.VoiceRecorderState,
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
    modifier: Modifier = Modifier,
    libraryTitle: String = ""
) {
    val speaking = LocalSpeakingColors.current

    // Стрелки в аппбаре нет (мокап frame-training) — системная кнопка/жест «назад»
    com.sotospeak.app.components.PlatformBackHandler(onBack = onBack)

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            // Мокап frame-training: h1 «Training», sub — «Тема · Топик», без стрелки назад
            SpeakingAppBar(
                title = "Training",
                subtitle = listOfNotNull(
                    libraryTitle.ifBlank { null },
                    state.topicTitle.ifBlank { null }
                ).joinToString(" · ").ifBlank { null }
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
    recorderState: com.sotospeak.app.recorder.VoiceRecorderState,
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
                // M3 AssistChip (A9): timer-цвета из LocalSpeakingColors
                AssistChip(
                    onClick = {},
                    modifier = Modifier.testTag("level_chip"),
                    label = {
                        Text(
                            "Уровень ${state.attemptNumber} · $limit сек",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = SpeakingIcons.Clock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = timerColor.copy(alpha = 0.15f),
                        labelColor = timerColor,
                        leadingIconContentColor = timerColor
                    ),
                    border = null
                )
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

        // Весь список вопросов — отвечаем на все одной записью (нумерация по мокапу .pq-item)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("training_questions_list")
            ) {
                state.questions.forEachIndexed { index, question ->
                    Card(
                        shape = SpeakingShapes.Chip,
                        colors = CardDefaults.cardColors(containerColor = speaking.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_item_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = speaking.primary,
                                modifier = Modifier.testTag("question_number_$index")
                            )
                            Text(
                                question.text,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified,
                                color = speaking.text
                            )
                        }
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
                        RecIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        // Таймер-кольцо (mockups.html .timer-ring, 176dp)
                        SpeakingTimerRing(
                            remainingSeconds = state.remainingSeconds,
                            totalSeconds = limit,
                            arcColor = timerColor,
                            timeText = formatTimer(state.remainingSeconds),
                            caption = "лимит попытки",
                            timerTestTag = "training_timer"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RecordingWaveform()
                        Spacer(modifier = Modifier.height(16.dp))
                        SpeakingRecordButton(
                            isRecording = true,
                            enabled = true,
                            onClick = onStopRecording,
                            testTag = "stop_button"
                        )
                    } else {
                        // T2: idle-кольцо видно ДО записи (полное кольцо, цвет уровня)
                        SpeakingTimerRing(
                            remainingSeconds = limit,
                            totalSeconds = limit,
                            arcColor = timerColor,
                            timeText = formatTimer(limit),
                            caption = "лимит попытки",
                            timerTestTag = "training_timer_idle"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SpeakingRecordButton(
                            isRecording = false,
                            enabled = micPermission == MicPermissionState.Granted,
                            onClick = onStartRecording,
                            testTag = "record_button"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // T5: подпись попытки по мокапу .rec-hint
                        Text(
                            "Попытка ${state.attemptNumber} · ответь на все вопросы одной записью",
                            style = MaterialTheme.typography.bodyMedium,
                            color = speaking.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("attempt_hint")
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
        // Заголовок по мокапу: «Попытки · N из 3» (всегда, даже при 0 — с подсказкой)
        item {
            Text(
                "Попытки · ${state.attempts.size} из ${TrainingViewModel.MAX_ATTEMPTS}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = speaking.text,
                modifier = Modifier.testTag("attempts_title")
            )
            if (state.attempts.isEmpty()) {
                Text(
                    "Записей пока нет — начни с попытки 1. Удалять записи нельзя, только прослушать.",
                    style = MaterialTheme.typography.bodySmall,
                    color = speaking.textMuted
                )
            }
        }
        if (state.attempts.isNotEmpty()) {
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

        // Privacy-note (mockups.html .privacy-note — shield-иконка)
        // M3 FilledCard (A9): container surfaceContainerLow
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = SpeakingIcons.Shield,
                        contentDescription = null,
                        tint = speaking.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Записи хранятся только на твоём устройстве",
                        style = MaterialTheme.typography.bodySmall,
                        color = speaking.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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

    // Декоративный прогресс воспроизведения (реальная позиция плеера недоступна,
    // симулируем как в mockups.html); сброс при остановке.
    val playedMs = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying && durationMs > 0) {
            playedMs.snapTo(0f)
            playedMs.animateTo(
                targetValue = durationMs.toFloat(),
                animationSpec = tween(durationMillis = durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), easing = LinearEasing)
            )
        } else {
            playedMs.snapTo(0f)
        }
    }
    val playedFraction = if (durationMs > 0) {
        (playedMs.value / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

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
                    imageVector = if (isPlaying) SpeakingIcons.Pause else SpeakingIcons.Play,
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
                if (isPlaying) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PlaybackWaveform(playedFraction = playedFraction)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // ✅ «принята» — автоматически, с checkPop (дизайн v1.1)
            CheckPopAppear {
                Icon(
                    imageVector = SpeakingIcons.CheckCircle,
                    contentDescription = "Принята",
                    tint = speaking.success,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("attempt_check_$index")
                )
            }
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
    // Появление финальных CTA — fadeIn + scale с EasingBounce (motion-токены)
    CheckPopAppear(initialScale = 0.9f) {
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
    libraryTitle: String,
    onNavigateToPractice: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: com.sotospeak.app.viewmodel.TrainingViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()
    val currentState by rememberUpdatedState(state)

    val recorder = remember { com.sotospeak.app.recorder.VoiceRecorder() }
    val recorderState by recorder.state.collectAsState()
    val currentRecorderState by rememberUpdatedState(recorderState)
    val recordingStore = org.koin.compose.koinInject<com.sotospeak.app.storage.RecordingStore>()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    val micPermission = com.sotospeak.app.recorder.rememberMicrophonePermissionState { granted ->
        vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnPermissionResult(granted))
    }
    LaunchedEffect(micPermission) {
        vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnPermissionState(micPermission))
    }

    LaunchedEffect(topicId) {
        vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnLoad(topicId))
    }

    com.sotospeak.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.sotospeak.app.viewmodel.TrainingEvent.NavigateToLibrary -> onNavigateToLibrary()
            is com.sotospeak.app.viewmodel.TrainingEvent.NavigateToPractice -> onNavigateToPractice()
            is com.sotospeak.app.viewmodel.TrainingEvent.NavigateBack -> onNavigateBack()
            is com.sotospeak.app.viewmodel.TrainingEvent.ShowMessage ->
                snackbarHostState.showSnackbar(event.text)
        }
    }

    // VoiceRecorder.state → VM
    LaunchedEffect(recorderState) {
        when (val rs = recorderState) {
            is com.sotospeak.app.recorder.VoiceRecorderState.Recording ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnRecorderStarted)
            is com.sotospeak.app.recorder.VoiceRecorderState.Stopped ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnRecorderStopped(rs.filePath))
            is com.sotospeak.app.recorder.VoiceRecorderState.Error ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnRecorderError(rs.message))
            else -> {}
        }
    }

    // Автостоп по таймеру
    LaunchedEffect(state.remainingSeconds) {
        if (state.recorder is com.sotospeak.app.viewmodel.RecorderUiState.Recording &&
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
                currentRecorderState is com.sotospeak.app.recorder.VoiceRecorderState.Recording
            ) {
                recorder.stop()
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnInterruption)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TrainingScreen(
            state = state,
            topicId = topicId,
            libraryTitle = libraryTitle,
            recorderState = recorderState,
            micPermission = micPermission,
            onStartRecording = {
                if (micPermission == com.sotospeak.app.recorder.MicPermissionState.Granted) {
                    vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnStartRecording)
                    val fileName = recordingStore.fileNameFor(
                        topicId = topicId,
                        kind = com.sotospeak.app.storage.RecordingKind.TRAINING,
                        attemptNumber = currentState.attemptNumber,
                        epochMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    )
                    recorder.start(fileName)
                }
            },
            onStopRecording = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnStopRecording)
                recorder.stop()
            },
            onPlayRecording = { path ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnPlayRecording(path))
            },
            onStopPlayback = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnStopPlayback)
            },
            onGoToPractice = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnGoToPractice)
            },
            onRestartAttempts = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnRestartAttempts)
            },
            onBackToLibrary = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnBackToLibrary)
            },
            onOpenSettings = { com.sotospeak.app.util.openAppSettings() },
            onRetry = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnLoad(topicId))
            },
            onBack = { vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnBack) }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
