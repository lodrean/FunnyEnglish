package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.viewmodel.RecorderUiState
import com.sotospeak.app.viewmodel.TrainingState
import com.sotospeak.app.viewmodel.TrainingViewModel
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

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
    recorderState: com.sotospeak.app.recorder.VoiceRecorderState,
    micPermission: MicPermissionState,
    onStartRecording: () -> Unit,       // экран: recorder.start(fileName)
    onStopRecording: () -> Unit,        // экран: recorder.stop()
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onToggleSelfCheck: (Int) -> Unit = {},   // §5.4: переключить критерий чеклиста
    onSaveSelfScore: () -> Unit = {},        // §5.4: «Готово»
    onSkipSelfScoring: () -> Unit = {},      // §5.4: «Пропустить»
    onOpenSelfScoring: (String) -> Unit = {}, // §5.4: повторное открытие из списка попыток
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

    // Back-кнопка аппбара (V1) и системная кнопка/жест ведут одинаково на onBack
    com.sotospeak.app.components.PlatformBackHandler(onBack = onBack)

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            // V1-макет: h1 «Тренировка», sub — «Тема · Топик», back-кнопка
            SpeakingAppBar(
                title = "Training",
                subtitle = listOfNotNull(
                    libraryTitle.ifBlank { null },
                    state.topicTitle.ifBlank { null }
                ).joinToString(" · ").ifBlank { null },
                onBack = onBack
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
                micPermission = micPermission,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onPlayRecording = onPlayRecording,
                onStopPlayback = onStopPlayback,
                onToggleSelfCheck = onToggleSelfCheck,
                onSaveSelfScore = onSaveSelfScore,
                onSkipSelfScoring = onSkipSelfScoring,
                onOpenSelfScoring = onOpenSelfScoring,
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
    micPermission: MicPermissionState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayRecording: (String) -> Unit,
    onStopPlayback: () -> Unit,
    onToggleSelfCheck: (Int) -> Unit,
    onSaveSelfScore: () -> Unit,
    onSkipSelfScoring: () -> Unit,
    onOpenSelfScoring: (String) -> Unit,
    onGoToPractice: () -> Unit,
    onRestartAttempts: () -> Unit,
    onBackToLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = state.recorder is RecorderUiState.Recording
    val limit = TrainingViewModel.timerLimitFor(state.attemptNumber)
    val timerColor = timerColorFor(state.attemptNumber)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Level-chip + индикаторы попыток
        item { LevelHeader(state = state, limit = limit, timerColor = timerColor) }

        // Весь список вопросов — отвечаем на все одной записью (нумерация по мокапу .pq-item)
        item { TrainingQuestionsList(questions = state.questions) }

        // Таймер + rec-кнопка (скрываются после 3-й попытки)
        if (!state.isFinished) {
            item {
                RecordZone(
                    state = state,
                    micPermission = micPermission,
                    isRecording = isRecording,
                    limit = limit,
                    timerColor = timerColor,
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording,
                    onOpenSettings = onOpenSettings
                )
            }
        }

        // Карточка самооценки после попытки (§5.4, bd h3l.5): между rec-зоной и списком
        // попыток; на 3-й попытке сосуществует с финальным блоком
        selfScoringSection(
            state = state,
            actions = SelfScoringActions(
                onPlayRecording = onPlayRecording,
                onStopPlayback = onStopPlayback,
                onToggleCheck = onToggleSelfCheck,
                onSave = onSaveSelfScore,
                onSkip = onSkipSelfScoring
            )
        )

        // Список попыток: только прослушивание, ✅ автоматически, удаления нет
        attemptsSection(
            state = state,
            onPlayRecording = onPlayRecording,
            onStopPlayback = onStopPlayback,
            onOpenSelfScoring = onOpenSelfScoring
        )

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
        item { PrivacyNote() }
    }
}

/** Таймер + rec-кнопка (idle/запись), rationale при отказе в микрофоне (§5.3). */
@Composable
private fun RecordZone(
    state: TrainingState,
    micPermission: MicPermissionState,
    isRecording: Boolean,
    limit: Int,
    timerColor: Color,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onOpenSettings: () -> Unit
) {
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
            RecordingControls(
                remainingSeconds = state.remainingSeconds,
                limit = limit,
                timerColor = timerColor,
                onStopRecording = onStopRecording
            )
        } else {
            IdleControls(
                state = state,
                micPermission = micPermission,
                limit = limit,
                timerColor = timerColor,
                onStartRecording = onStartRecording
            )
        }
    }
}

/** Активная запись: пульс + таймер-кольцо + waveform + стоп (§5.3). */
@Composable
private fun RecordingControls(
    remainingSeconds: Int,
    limit: Int,
    timerColor: Color,
    onStopRecording: () -> Unit
) {
    val strings = LocalAppStrings.current
    RecIndicator()
    Spacer(modifier = Modifier.height(8.dp))
    // Таймер-кольцо (mockups.html .timer-ring, 176dp)
    SpeakingTimerRing(
        remainingSeconds = remainingSeconds,
        totalSeconds = limit,
        arcColor = timerColor,
        timeText = formatTimer(remainingSeconds),
        caption = strings.captionAttemptLimit,
        timerTestTag = "training_timer"
    )
    Spacer(modifier = Modifier.height(8.dp))
    RecordingWaveform()
    Spacer(modifier = Modifier.height(16.dp))
    SpeakingRecordButton(
        isRecording = true,
        enabled = true,
        onClick = onStopRecording,
        testTag = "stop_button",
        contentDescription = strings.stopRecordingDesc
    )
}

/** Idle: полное кольцо лимита + record-кнопка + подпись попытки (§5.3). */
@Composable
private fun IdleControls(
    state: TrainingState,
    micPermission: MicPermissionState,
    limit: Int,
    timerColor: Color,
    onStartRecording: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    // T2: idle-кольцо видно ДО записи (полное кольцо, цвет уровня)
    SpeakingTimerRing(
        remainingSeconds = limit,
        totalSeconds = limit,
        arcColor = timerColor,
        timeText = formatTimer(limit),
        caption = strings.captionAttemptLimit,
        timerTestTag = "training_timer_idle"
    )
    Spacer(modifier = Modifier.height(16.dp))
    SpeakingRecordButton(
        isRecording = false,
        enabled = micPermission == MicPermissionState.Granted,
        onClick = onStartRecording,
        testTag = "record_button",
        contentDescription = strings.startRecordingDesc
    )
    Spacer(modifier = Modifier.height(8.dp))
    // T5: подпись попытки по мокапу .rec-hint
    Text(
        strings.attemptHint(state.attemptNumber),
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

@Composable
internal fun AttemptCard(
    index: Int,
    durationMs: Long,
    isPlaying: Boolean,
    selfScoreSummary: String?,           // «N/4», если самооценка сохранена (§5.4)
    onPlay: () -> Unit,
    onOpenSelfScoring: () -> Unit        // тап по попытке → карточка самооценки
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current

    // Декоративный прогресс воспроизведения — общая симуляция (rememberPlayedFraction)
    val playedFraction = rememberPlayedFraction(durationMs = durationMs, isPlaying = isPlaying)

    Card(
        onClick = onOpenSelfScoring,
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
                    contentDescription = if (isPlaying) strings.playbackStopDesc else strings.playbackListenDesc,
                    tint = speaking.waveformPlayback
                )
            }
            AttemptInfo(
                index = index,
                durationMs = durationMs,
                isPlaying = isPlaying,
                playedFraction = playedFraction,
                selfScoreSummary = selfScoreSummary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // ✅ «принята» — автоматически, с checkPop (дизайн v1.1)
            CheckPopAppear {
                Icon(
                    imageVector = SpeakingIcons.CheckCircle,
                    contentDescription = strings.attemptAcceptedDesc,
                    tint = speaking.success,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("attempt_check_$index")
                )
            }
        }
    }
}

/** Инфо-колонка попытки: номер, длительность, waveform при игре, сводка самооценки. */
@Composable
private fun AttemptInfo(
    index: Int,
    durationMs: Long,
    isPlaying: Boolean,
    playedFraction: Float,
    selfScoreSummary: String?,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    Column(modifier = modifier) {
        Text(
            strings.attemptNumber(index + 1),
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
        if (selfScoreSummary != null) {
            Spacer(modifier = Modifier.height(6.dp))
            SelfScoreSummaryChip(summary = selfScoreSummary, index = index)
        }
    }
}


@Composable
internal fun MicPermissionRationale(
    permanentlyDenied: Boolean,
    onOpenSettings: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
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
                strings.micPermissionTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = speaking.text
            )
            if (permanentlyDenied) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenSettings) {
                    Text(strings.openSettings)
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
    // VM со скоупом маршрута (К3): на каждый topicId своя VM, очищается при уходе с экрана
    val vm: com.sotospeak.app.viewmodel.TrainingViewModel =
        com.sotospeak.app.util.routeViewModel("training:$topicId")
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
            onToggleSelfCheck = { index ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnToggleSelfCheck(index))
            },
            onSaveSelfScore = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnSaveSelfScore)
            },
            onSkipSelfScoring = {
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnSkipSelfScoring)
            },
            onOpenSelfScoring = { path ->
                vm.onAction(com.sotospeak.app.viewmodel.TrainingAction.OnOpenSelfScoring(path))
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
