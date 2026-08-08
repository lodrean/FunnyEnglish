package com.sotospeak.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.player.NativeVideoSurface
import com.sotospeak.app.player.VideoPlayerController
import com.sotospeak.app.player.VideoPlayerState
import com.sotospeak.app.subtitles.SubtitlePanel
import com.sotospeak.app.viewmodel.VideoState
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

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

    // Подготовка плеера при появлении URL видео; reloadNonce — перезапуск после retry
    val videoUrl = state.topic?.video?.videoUrl
    LaunchedEffect(videoUrl, state.reloadNonce) {
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
                // M3 FilterChip (A7): selected → primaryContainer/onPrimaryContainer,
                // unselected → outline outlineVariant (DSM-5 §4)
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                val chipBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                FilterChip(
                    selected = state.subtitlesEnabled,
                    onClick = { if (!state.subtitlesEnabled) onToggleSubtitles() },
                    label = { Text("С субтитрами") },
                    leadingIcon = {
                        Icon(
                            imageVector = SpeakingIcons.ClosedCaptions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = chipColors,
                    border = if (state.subtitlesEnabled) null else chipBorder,
                    modifier = Modifier.testTag("subtitles_toggle")
                )
                FilterChip(
                    selected = !state.subtitlesEnabled,
                    onClick = { if (state.subtitlesEnabled) onToggleSubtitles() },
                    label = { Text("Без субтитров") },
                    colors = chipColors,
                    border = if (!state.subtitlesEnabled) null else chipBorder
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

            // V1: кастомные контролы мокапа (big-play + control-bar), нативные выключены
            if (!state.videoError) {
                MockupVideoControls(
                    playerState = playerState,
                    subtitlesOn = state.subtitlesEnabled && hasSubtitles,
                    onPlayPause = { if (playerState.isPlaying) controller.pause() else controller.play() },
                    onSeek = { fraction ->
                        if (playerState.durationMs > 0) {
                            controller.seekTo((playerState.durationMs * fraction).toLong())
                        }
                    },
                    onToggleCc = onToggleSubtitles
                )
            }

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
        // M3 FilledButton (A7/DSM-5 C1): shape medium(16), container primary (=primaryStrong)
        Button(
            onClick = onGoToQuestions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
                .testTag("go_to_questions_button"),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Перейти к вопросам", fontWeight = FontWeight.SemiBold)
        }
        // V4: подсказка мокапа (.video-hint)
        Text(
            "Смотреть всё видео необязательно — к вопросам можно перейти в любой момент",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("video_hint")
        )
    }
}

/**
 * Контролы плеера по мокапу frame-video:
 * big-play 64dp по центру (скрывается при воспроизведении),
 * control-bar внизу (play/pause, seek, время 0:00 / 1:35, CC с подсветкой #FFD666).
 */
@Composable
private fun MockupVideoControls(
    playerState: VideoPlayerState,
    subtitlesOn: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleCc: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // «Начать заново» после окончания воспроизведения (STATE_ENDED)
        if (playerState.isEnded) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        onSeek(0f)
                        onPlayPause()
                    },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("replay_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SpeakingIcons.Refresh,
                            contentDescription = "Начать заново",
                            tint = Color(0xFF1A2E42),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "Начать заново",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.testTag("replay_label")
                )
            }
        }

        // Big-play (mockups .big-play): круг 64dp, белый 92%, тень;
        // H: появление/исчезание fade+scale tweenFast
        androidx.compose.animation.AnimatedVisibility(
            visible = !playerState.isPlaying && !playerState.isEnded,
            enter = androidx.compose.animation.fadeIn(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()) +
                androidx.compose.animation.scaleIn(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()),
            exit = androidx.compose.animation.fadeOut(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()) +
                androidx.compose.animation.scaleOut(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                onClick = onPlayPause,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("big_play_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = SpeakingIcons.Play,
                        contentDescription = "Смотреть видео",
                        tint = Color(0xFF1A2E42),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Control-bar (mockups .video-controls): градиентная подложка снизу
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
                .testTag("video_control_bar")
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(48.dp)   // аудит: touch target 48
                    .testTag("vc_play_pause")
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) SpeakingIcons.Pause else SpeakingIcons.Play,
                    contentDescription = if (playerState.isPlaying) "Пауза" else "Продолжить",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            // Seek (mockups .vc-seek)
            val fraction = if (playerState.durationMs > 0) {
                (playerState.positionMs.toFloat() / playerState.durationMs).coerceIn(0f, 1f)
            } else 0f
            Slider(
                value = fraction,
                onValueChange = onSeek,
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .testTag("vc_seek"),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            Text(
                "${formatTimer((playerState.positionMs / 1000).toInt())} / ${formatTimer((playerState.durationMs / 1000).toInt())}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.testTag("vc_time")
            )
            // CC (mockups .vc-btn.cc.on — подсветка #FFD666 снизу)
            Surface(
                onClick = onToggleCc,
                shape = SpeakingShapes.Chip,
                color = if (subtitlesOn) Color.White.copy(alpha = 0.22f) else Color.Transparent,
                modifier = Modifier
                    .height(48.dp)   // аудит: touch target 48
                    .testTag("vc_cc")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        "CC",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (subtitlesOn) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .clip(SpeakingShapes.StatusPill)
                                .background(Color(0xFFFFD666))
                        )
                    }
                }
            }
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
    val vm: com.sotospeak.app.viewmodel.VideoViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()

    val controller = remember { VideoPlayerController() }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    LaunchedEffect(topicId, withSubtitles) {
        vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnLoad(topicId, withSubtitles))
    }

    com.sotospeak.app.util.ObserveAsEvents(vm.events) { event ->
        when (event) {
            is com.sotospeak.app.viewmodel.VideoEvent.NavigateToQuestions -> onNavigateToQuestions()
            is com.sotospeak.app.viewmodel.VideoEvent.NavigateBack -> onNavigateBack()
        }
    }

    VideoScreen(
        state = state,
        controller = controller,
        onToggleSubtitles = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnToggleSubtitles) },
        onVideoStarted = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnVideoStarted(topicId)) },
        onVideoError = { vm.onVideoError() },
        onRetryVideo = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnRetryVideo) },
        onGoToQuestions = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnGoToQuestions) },
        onBack = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnBack) }
    )
}
