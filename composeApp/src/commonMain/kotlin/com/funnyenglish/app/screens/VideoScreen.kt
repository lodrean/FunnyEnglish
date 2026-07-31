package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.player.NativeVideoSurface
import com.funnyenglish.app.player.VideoPlayerController
import com.funnyenglish.app.player.VideoPlayerState
import com.funnyenglish.app.subtitles.SubtitlePanel
import com.funnyenglish.app.viewmodel.VideoState
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes

/**
 * Экран видео топика (спека Part 2 §2.3, §3.4).
 * Mode-chips «С субтитрами/Без субтитров» + плеер + SubtitlePanel под плеером.
 * CTA «Перейти к вопросам» доступен всегда (PRD Story 2).
 *
 * Контроллер плеера — remember + DisposableEffect (НЕ в Koin, спека §8.1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    state: VideoState,
    controller: VideoPlayerController,
    onToggleSubtitles: () -> Unit,
    onVideoStarted: () -> Unit,
    onVideoError: () -> Unit,
    onRetryVideo: () -> Unit,
    onGoToQuestions: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val playerState by controller.state.collectAsState()

    // Подготовка плеера при появлении URL видео
    val videoUrl = state.topic?.video?.videoUrl
    LaunchedEffect(videoUrl) {
        if (videoUrl != null) controller.prepare(videoUrl)
    }

    // Ошибка плеера → плашка (PRD Edge Cases); старт воспроизведения → флаг watched
    LaunchedEffect(playerState.error) {
        if (playerState.error != null) onVideoError()
    }
    var watchedMarked by remember { mutableStateOf(false) }
    LaunchedEffect(playerState.isPlaying) {
        if (playerState.isPlaying && !watchedMarked) {
            watchedMarked = true
            onVideoStarted()
        }
    }

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.topic?.title.orEmpty(),
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
        modifier = modifier.testTag("video_screen")
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.error != null -> ErrorMessage(
                message = state.error,
                onRetry = onRetryVideo,
                modifier = Modifier.padding(padding)
            )
            else -> VideoContent(
                state = state,
                playerState = playerState,
                controller = controller,
                onToggleSubtitles = onToggleSubtitles,
                onRetryVideo = onRetryVideo,
                onGoToQuestions = onGoToQuestions,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun VideoContent(
    state: VideoState,
    playerState: VideoPlayerState,
    controller: VideoPlayerController,
    onToggleSubtitles: () -> Unit,
    onRetryVideo: () -> Unit,
    onGoToQuestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val hasSubtitles = state.topic?.video?.subtitleUrl != null

    Column(modifier = modifier.fillMaxSize()) {
        // Mode-chips (видны только если у топика есть субтитры)
        if (hasSubtitles) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.subtitlesEnabled,
                    onClick = { if (!state.subtitlesEnabled) onToggleSubtitles() },
                    label = { Text("С субтитрами") },
                    modifier = Modifier.testTag("subtitles_toggle")
                )
                FilterChip(
                    selected = !state.subtitlesEnabled,
                    onClick = { if (state.subtitlesEnabled) onToggleSubtitles() },
                    label = { Text("Без субтитров") }
                )
            }
        }

        // Плеер
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(androidx.compose.ui.graphics.Color.Black)
                .testTag("video_surface")
        ) {
            NativeVideoSurface(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )

            // Плашка ошибки видео: retry + «К вопросам» (переход без видео разрешён)
            if (state.videoError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(speaking.scrimVideoControls)
                        .testTag("video_error")
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Не удалось загрузить видео",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onRetryVideo) {
                            Text("Повторить", color = androidx.compose.ui.graphics.Color.White)
                        }
                        Button(onClick = onGoToQuestions) {
                            Text("К вопросам")
                        }
                    }
                }
            }
        }

        // Субтитры ПОД плеером
        if (state.subtitlesEnabled && state.subtitleCues.isNotEmpty()) {
            SubtitlePanel(
                cues = state.subtitleCues,
                positionMs = playerState.positionMs
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // CTA доступен всегда — смотреть всё видео необязательно
        Button(
            onClick = onGoToQuestions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .testTag("go_to_questions_button"),
            shape = SpeakingShapes.Card,
            colors = ButtonDefaults.buttonColors(containerColor = speaking.primary)
        ) {
            Text("Перейти к вопросам", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ==================== Route (связка VM + контроллер плеера) ====================

/**
 * Обёртка экрана видео: создаёт VideoViewModel (Koin) и VideoPlayerController
 * (remember + DisposableEffect, НЕ в Koin — спека §8.1).
 */
@Composable
fun VideoRoute(
    topicId: String,
    withSubtitles: Boolean,
    onNavigateToQuestions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: com.funnyenglish.app.viewmodel.VideoViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()

    val controller = remember { VideoPlayerController() }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    LaunchedEffect(topicId, withSubtitles) {
        vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnLoad(topicId, withSubtitles))
    }

    com.funnyenglish.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.funnyenglish.app.viewmodel.VideoEvent.NavigateToQuestions -> onNavigateToQuestions()
            is com.funnyenglish.app.viewmodel.VideoEvent.NavigateBack -> onNavigateBack()
        }
    }

    VideoScreen(
        state = state,
        controller = controller,
        onToggleSubtitles = { vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnToggleSubtitles) },
        onVideoStarted = { vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnVideoStarted(topicId)) },
        onVideoError = { vm.onVideoError() },
        onRetryVideo = { vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnRetryVideo) },
        onGoToQuestions = { vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnGoToQuestions) },
        onBack = { vm.onAction(com.funnyenglish.app.viewmodel.VideoAction.OnBack) }
    )
}
