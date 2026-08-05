package com.sotospeak.app.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.CheckPopAppear
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.components.RecIndicator
import com.sotospeak.app.components.RecordingWaveform
import com.sotospeak.app.components.SpeakingRecordButton
import com.sotospeak.app.components.SpeakingTimerRing
import com.sotospeak.app.recorder.MicPermissionState
import com.sotospeak.app.viewmodel.PracticePhase
import com.sotospeak.app.viewmodel.PracticeState
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.accessibility.LocalReduceMotion
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.designsystem.theme.SpeakingTextStyles

/**
 * Экран Practice (спека Part 2 §2.6, §6.1; дизайн Playful Coach v1.1):
 * 30 секунд, одна запись на ВСЕ вопросы, БЕЗ Review —
 * автостоп/ручной стоп/прерывание → автоматическая отправка учителю.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    state: PracticeState,
    onStart: () -> Unit,                // экран: recorder.start(...)
    onStopEarly: () -> Unit,            // экран: recorder.stop() → автоотправка
    onRetryUpload: () -> Unit,
    onBackToLibrary: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    // «Назад» в фазах Recording/Uploading — диалог-подтверждение (спека §6.1)
    var showBackConfirm by remember { mutableStateOf(false) }
    val handleBack = {
        if (state.phase == PracticePhase.Recording || state.phase == PracticePhase.Uploading) {
            showBackConfirm = true
        } else {
            onBack()
        }
    }

    Scaffold(
        containerColor = speaking.background,
        // P1: заголовок «Practice» + тема подзаголовком (мокап appbar)
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Practice", fontWeight = FontWeight.Bold, maxLines = 1)
                        if (state.topicTitle.isNotBlank()) {
                            Text(
                                state.topicTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = speaking.textMuted,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
            )
        },
        modifier = modifier.testTag("practice_screen")
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.error != null && state.phase == PracticePhase.Ready && !state.uploadError ->
                ErrorMessage(message = state.error, onRetry = onRetry, modifier = Modifier.padding(padding))
            else -> PracticeContent(
                state = state,
                onStart = onStart,
                onStopEarly = onStopEarly,
                onRetryUpload = onRetryUpload,
                onBackToLibrary = onBackToLibrary,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text("Прервать запись?") },
            text = {
                Text(
                    if (state.phase == PracticePhase.Recording)
                        "Запись будет потеряна."
                    else
                        "Отправка прервётся — запись останется на устройстве."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    onBack()
                }) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) { Text("Остаться") }
            }
        )
    }
}

@Composable
private fun PracticeContent(
    state: PracticeState,
    onStart: () -> Unit,
    onStopEarly: () -> Unit,
    onRetryUpload: () -> Unit,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Чипы режима (P5: «Контрольная · 30 сек» peach #FBEAE8/#B3261E + «1 ЗАПИСЬ…» chip-new)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = speaking.recordContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = SpeakingIcons.Clock,
                            contentDescription = null,
                            tint = speaking.onRecordContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Контрольная · 30 сек",
                            color = speaking.onRecordContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = speaking.statusNewContainer
                ) {
                    Text(
                        "1 ЗАПИСЬ НА ВСЕ ВОПРОСЫ",
                        color = speaking.statusNew,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Список всех вопросов (P3: нумерация по мокапу .pq-item)
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("practice_questions_list")
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
                                color = speaking.text
                            )
                        }
                    }
                }
            }
        }

        // Фазовая зона
        item {
            when (state.phase) {
                PracticePhase.Ready -> ReadyPhase(
                    state = state,
                    onStart = onStart,
                    onRetryUpload = onRetryUpload
                )
                PracticePhase.Recording -> RecordingPhase(
                    remainingSeconds = state.remainingSeconds,
                    onStopEarly = onStopEarly
                )
                PracticePhase.Uploading -> UploadingPhase(progress = state.uploadProgress)
                PracticePhase.Sent -> SentPhase(onBackToLibrary = onBackToLibrary)
            }
        }

        // P4: плашка автоотправки — жёлтая (.practice-note), ВНИЗУ экрана, текст мокапа
        item {
            Card(
                shape = SpeakingShapes.Chip,
                colors = CardDefaults.cardColors(containerColor = speaking.statusNewContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_auto_send_note")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = SpeakingIcons.Upload,
                        contentDescription = null,
                        tint = speaking.statusNew,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "В отличие от Training, эта запись уйдёт учителю автоматически сразу после остановки таймера — изменить её нельзя",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.statusNew
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyPhase(
    state: PracticeState,
    onStart: () -> Unit,
    onRetryUpload: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.uploadError) {
            // Файл не потерян — retry (спека §6.4)
            Card(
                shape = SpeakingShapes.Card,
                colors = CardDefaults.cardColors(containerColor = speaking.statusNewContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Не удалось отправить. Запись сохранена на устройстве.",
                        color = speaking.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRetryUpload,
                        modifier = Modifier.testTag("upload_retry_button")
                    ) {
                        Text("Повторить отправку")
                    }
                }
            }
        }
        if (state.micPermission == MicPermissionState.Denied ||
            state.micPermission == MicPermissionState.PermanentlyDenied
        ) {
            Text(
                "Для записи нужен доступ к микрофону",
                color = speaking.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Таймер-кольцо видно и до записи — полные 30 сек (mockups.html, 150dp)
        SpeakingTimerRing(
            remainingSeconds = PracticeState.PRACTICE_LIMIT_SECONDS,
            totalSeconds = PracticeState.PRACTICE_LIMIT_SECONDS,
            arcColor = speaking.timerLevel30,
            timeText = formatTimer(PracticeState.PRACTICE_LIMIT_SECONDS),
            size = 150.dp,
            timerTextStyle = SpeakingTextStyles.TimerDisplay.copy(fontSize = 40.sp, lineHeight = 44.sp),
            caption = "на все ответы"
        )
        Spacer(modifier = Modifier.height(16.dp))
        // P2: большая record-кнопка мокапа (.rec-btn)
        SpeakingRecordButton(
            isRecording = false,
            enabled = state.micPermission == MicPermissionState.Granted,
            onClick = onStart,
            testTag = "practice_start_button"
        )
    }
}

@Composable
private fun RecordingPhase(
    remainingSeconds: Int,
    onStopEarly: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        // Таймер-кольцо 150dp (mockups.html practice)
        SpeakingTimerRing(
            remainingSeconds = remainingSeconds,
            totalSeconds = PracticeState.PRACTICE_LIMIT_SECONDS,
            arcColor = speaking.timerLevel30,
            timeText = formatTimer(remainingSeconds),
            size = 150.dp,
            timerTextStyle = SpeakingTextStyles.TimerDisplay.copy(fontSize = 40.sp, lineHeight = 44.sp),
            caption = "на все ответы",
            timerTestTag = "practice_timer"
        )
        Spacer(modifier = Modifier.height(8.dp))
        RecordingWaveform()
        Spacer(modifier = Modifier.height(16.dp))
        // P2: большая record-кнопка мокапа (.rec-btn, стоп → автоотправка)
        SpeakingRecordButton(
            isRecording = true,
            enabled = true,
            onClick = onStopEarly,
            testTag = "practice_stop_button"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Закончить и отправить",
            style = MaterialTheme.typography.bodyMedium,
            color = speaking.textMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UploadingPhase(progress: Int) {
    val speaking = LocalSpeakingColors.current
    val reduceMotion = LocalReduceMotion.current
    // Прогресс отправки — tween 180ms linear по ширине (mockups.html .upload-track)
    val animatedProgress by animateFloatAsState(
        targetValue = (progress / 100f).coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) snap() else tween(durationMillis = 180, easing = LinearEasing),
        label = "upload_progress"
    )
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_panel")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Отправка учителю…",
                style = MaterialTheme.typography.titleMedium,
                color = speaking.text
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = speaking.primary,
                trackColor = speaking.surfaceVariant
            )
        }
    }
}

@Composable
private fun SentPhase(onBackToLibrary: () -> Unit) {
    val speaking = LocalSpeakingColors.current
    Card(
        shape = SpeakingShapes.Card,
        colors = CardDefaults.cardColors(containerColor = speaking.statusReviewedContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sent_panel")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sent-бейдж 64dp: круг statusReviewedContainer + check с checkPop (mockups.html .sent-badge)
            CheckPopAppear {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(speaking.statusReviewedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        SpeakingIcons.CheckCircle,
                        contentDescription = null,
                        tint = speaking.statusReviewed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Запись отправлена!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = speaking.text
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = SpeakingShapes.StatusPill,
                color = speaking.statusNewContainer
            ) {
                Text(
                    "статус NEW · ждёт проверки",
                    color = speaking.statusNew,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Оценка и комментарий появятся в «Отправки»",
                style = MaterialTheme.typography.bodySmall,
                color = speaking.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onBackToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sent_back_button")
            ) {
                Text("Вернуться в библиотеку")
            }
        }
    }
}



// ==================== Route (связка VM + VoiceRecorder + permission + lifecycle) ====================

/**
 * Обёртка Practice-экрана (спека §4.4, §6, §8.1).
 * Гейтинг гостя — двойной: QuestionsScreen (CTA) + проверка токена в VM (§6.2).
 */
@Composable
fun PracticeRoute(
    topicId: String,
    onNavigateToMySubmissions: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: com.sotospeak.app.viewmodel.PracticeViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()

    val recorder = remember { com.sotospeak.app.recorder.VoiceRecorder() }
    val recorderState by recorder.state.collectAsState()
    val currentRecorderState by rememberUpdatedState(recorderState)
    val recordingStore = org.koin.compose.koinInject<com.sotospeak.app.storage.RecordingStore>()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    val micPermission = com.sotospeak.app.recorder.rememberMicrophonePermissionState { granted ->
        vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnPermissionResult(granted))
    }
    LaunchedEffect(micPermission) {
        vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnPermissionState(micPermission))
    }

    LaunchedEffect(topicId) {
        vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnLoad(topicId))
    }

    com.sotospeak.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.sotospeak.app.viewmodel.PracticeEvent.NavigateToMySubmissions ->
                onNavigateToMySubmissions()
            is com.sotospeak.app.viewmodel.PracticeEvent.NavigateBack -> onNavigateBack()
            is com.sotospeak.app.viewmodel.PracticeEvent.ShowMessage ->
                snackbarHostState.showSnackbar(event.text)
        }
    }

    // VoiceRecorder.state → VM (Stopped → автоотправка в VM, спека §6.1)
    LaunchedEffect(recorderState) {
        when (val rs = recorderState) {
            is com.sotospeak.app.recorder.VoiceRecorderState.Recording ->
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnRecorderStarted)
            is com.sotospeak.app.recorder.VoiceRecorderState.Stopped ->
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnRecorderStopped(rs.filePath))
            is com.sotospeak.app.recorder.VoiceRecorderState.Error ->
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnRecorderError(rs.message))
            else -> {}
        }
    }

    // Автостоп на 0:00 (PRD Story 5)
    LaunchedEffect(state.remainingSeconds) {
        if (state.phase == com.sotospeak.app.viewmodel.PracticePhase.Recording &&
            state.remainingSeconds <= 0
        ) {
            recorder.stop()
        }
    }

    // Прерывание → stop → автоотправка (PRD Edge Cases)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE &&
                currentRecorderState is com.sotospeak.app.recorder.VoiceRecorderState.Recording
            ) {
                recorder.stop()
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnInterruption)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PracticeScreen(
            state = state,
            onStart = {
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnStart)
                if (micPermission == com.sotospeak.app.recorder.MicPermissionState.Granted &&
                    vm.state.value.phase == com.sotospeak.app.viewmodel.PracticePhase.Recording
                ) {
                    val fileName = recordingStore.fileNameFor(
                        topicId = topicId,
                        kind = com.sotospeak.app.storage.RecordingKind.PRACTICE,
                        attemptNumber = 0,
                        epochMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    )
                    recorder.start(fileName)
                }
            },
            onStopEarly = {
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnStopEarly)
                recorder.stop()
            },
            onRetryUpload = {
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnRetryUpload)
            },
            onBackToLibrary = onNavigateToLibrary,
            onRetry = {
                vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnLoad(topicId))
            },
            onBack = { vm.onAction(com.sotospeak.app.viewmodel.PracticeAction.OnBack) }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
