package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.recorder.MicPermissionState
import com.funnyenglish.app.viewmodel.PracticePhase
import com.funnyenglish.app.viewmodel.PracticeState
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes
import com.funnyenglish.designsystem.theme.SpeakingTextStyles

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.topicTitle.ifBlank { "Практика" },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
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
        // Чипы режима
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = speaking.record.copy(alpha = 0.15f)
                ) {
                    Text(
                        "Контрольная · 30 сек",
                        color = speaking.record,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    shape = SpeakingShapes.StatusPill,
                    color = speaking.secondaryContainer
                ) {
                    Text(
                        "1 запись на все вопросы",
                        color = speaking.text,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Плашка автоотправки
        item {
            Card(
                shape = SpeakingShapes.Card,
                colors = CardDefaults.cardColors(containerColor = speaking.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_auto_send_note")
            ) {
                Text(
                    "Запись уйдёт учителю автоматически — изменить её нельзя",
                    style = MaterialTheme.typography.bodyMedium,
                    color = speaking.text,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Список всех вопросов
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("practice_questions_list")
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
        Button(
            onClick = onStart,
            enabled = state.micPermission == MicPermissionState.Granted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("practice_start_button"),
            shape = SpeakingShapes.Recorder,
            colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                tint = speaking.onRecord
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Начать запись",
                fontWeight = FontWeight.SemiBold,
                color = speaking.onRecord
            )
        }
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
        Text(
            formatTimer(remainingSeconds),
            style = SpeakingTextStyles.TimerDisplay,
            color = speaking.timerLevel30,
            modifier = Modifier.testTag("practice_timer")
        )
        LinearProgressIndicator(
            progress = {
                (remainingSeconds / PracticeState.PRACTICE_LIMIT_SECONDS.toFloat()).coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            color = speaking.timerLevel30,
            trackColor = speaking.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStopEarly,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("practice_stop_button"),
            shape = SpeakingShapes.Recorder,
            colors = ButtonDefaults.buttonColors(containerColor = speaking.record)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null, tint = speaking.onRecord)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Закончить и отправить", color = speaking.onRecord)
        }
    }
}

@Composable
private fun UploadingPhase(progress: Int) {
    val speaking = LocalSpeakingColors.current
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
                progress = { progress / 100f },
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
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = speaking.statusReviewed,
                modifier = Modifier.size(48.dp)
            )
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
                "Оценка и комментарий появятся в «Мои записи»",
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
    val vm: com.funnyenglish.app.viewmodel.PracticeViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()

    val recorder = remember { com.funnyenglish.app.recorder.VoiceRecorder() }
    val recorderState by recorder.state.collectAsState()
    val currentRecorderState by rememberUpdatedState(recorderState)
    val recordingStore = org.koin.compose.koinInject<com.funnyenglish.app.storage.RecordingStore>()

    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    val micPermission = com.funnyenglish.app.recorder.rememberMicrophonePermissionState { granted ->
        vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnPermissionResult(granted))
    }
    LaunchedEffect(micPermission) {
        vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnPermissionState(micPermission))
    }

    LaunchedEffect(topicId) {
        vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnLoad(topicId))
    }

    com.funnyenglish.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.funnyenglish.app.viewmodel.PracticeEvent.NavigateToMySubmissions ->
                onNavigateToMySubmissions()
            is com.funnyenglish.app.viewmodel.PracticeEvent.NavigateBack -> onNavigateBack()
            is com.funnyenglish.app.viewmodel.PracticeEvent.ShowMessage ->
                snackbarHostState.showSnackbar(event.text)
        }
    }

    // VoiceRecorder.state → VM (Stopped → автоотправка в VM, спека §6.1)
    LaunchedEffect(recorderState) {
        when (val rs = recorderState) {
            is com.funnyenglish.app.recorder.VoiceRecorderState.Recording ->
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnRecorderStarted)
            is com.funnyenglish.app.recorder.VoiceRecorderState.Stopped ->
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnRecorderStopped(rs.filePath))
            is com.funnyenglish.app.recorder.VoiceRecorderState.Error ->
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnRecorderError(rs.message))
            else -> {}
        }
    }

    // Автостоп на 0:00 (PRD Story 5)
    LaunchedEffect(state.remainingSeconds) {
        if (state.phase == com.funnyenglish.app.viewmodel.PracticePhase.Recording &&
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
                currentRecorderState is com.funnyenglish.app.recorder.VoiceRecorderState.Recording
            ) {
                recorder.stop()
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnInterruption)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PracticeScreen(
            state = state,
            onStart = {
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnStart)
                if (micPermission == com.funnyenglish.app.recorder.MicPermissionState.Granted &&
                    vm.state.value.phase == com.funnyenglish.app.viewmodel.PracticePhase.Recording
                ) {
                    val fileName = recordingStore.fileNameFor(
                        topicId = topicId,
                        kind = com.funnyenglish.app.storage.RecordingKind.PRACTICE,
                        attemptNumber = 0,
                        epochMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    )
                    recorder.start(fileName)
                }
            },
            onStopEarly = {
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnStopEarly)
                recorder.stop()
            },
            onRetryUpload = {
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnRetryUpload)
            },
            onBackToLibrary = onNavigateToLibrary,
            onRetry = {
                vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnLoad(topicId))
            },
            onBack = { vm.onAction(com.funnyenglish.app.viewmodel.PracticeAction.OnBack) }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
