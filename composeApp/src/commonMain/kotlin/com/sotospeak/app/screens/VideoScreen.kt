package com.sotospeak.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.player.NativeVideoSurface
import com.sotospeak.app.player.VideoFullscreenEffect
import com.sotospeak.app.player.VideoPlayerController
import com.sotospeak.app.player.VideoPlayerState
import com.sotospeak.app.subtitles.TranscriptPanel
import com.sotospeak.app.viewmodel.VideoState
import com.sotospeak.design.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

/**
 * Экран видео топика (спека Part 2 §2.3, §3.4).
 * Mode-chips «С субтитрами/Без субтитров» + плеер + TranscriptPanel (полный
 * транскрипт с пословной подсветкой) под плеером.
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
    modifier: Modifier = Modifier,
    libraryTitle: String = ""
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

    // Полноэкранный режим (спека §2.3, v1.7) — состояние локально для экрана, НЕ в VM
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    VideoFullscreenEffect(enabled = isFullscreen)

    // Стрелки в аппбаре нет (мокап frame-video) — системная кнопка/жест «назад»;
    // в fullscreen «назад» сначала выходит из полноэкранного режима
    com.sotospeak.app.components.PlatformBackHandler(
        onBack = { if (isFullscreen) isFullscreen = false else onBack() }
    )

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            // Мокап frame-video: h1 — название топика, sub — «Тема · видео m:ss», БЕЗ стрелки назад;
            // в fullscreen аппбар скрывается
            if (!isFullscreen) {
                val durationSec = state.topic?.video?.durationSeconds
                SpeakingAppBar(
                    title = state.topic?.title.orEmpty(),
                    subtitle = listOfNotNull(
                        libraryTitle.ifBlank { null },
                        durationSec?.let { "видео ${formatTimer(it)}" }
                    ).joinToString(" · ").ifBlank { null }
                )
            }
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
                isFullscreen = isFullscreen,
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onToggleSubtitles = onToggleSubtitles,
                onRetryVideo = onRetryVideo,
                onGoToQuestions = onGoToQuestions,
                // В fullscreen Scaffold-insets игнорируем — видео edge-to-edge
                modifier = if (isFullscreen) Modifier else Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun VideoContent(
    state: VideoState,
    playerState: VideoPlayerState,
    controller: VideoPlayerController,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onRetryVideo: () -> Unit,
    onGoToQuestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val hasSubtitles = state.topic?.video?.subtitleUrl != null

    // BoxWithConstraints: высота видео ограничена долей экрана — на низких viewport'ах
    // (1280x720, landscape) полноширинный 16:9 вытеснял транскрипт и CTA за экран
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val videoMaxHeight = maxHeight * 0.45f

        Column(modifier = Modifier.fillMaxSize()) {
        // Mode-chips (видны только если у топика есть субтитры; в fullscreen скрыты)
        if (!isFullscreen && hasSubtitles) {
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

        // Плеер: обычный режим — 16:9, но не выше 45% высоты экрана (letterbox по центру);
        // fullscreen — на весь экран (Android) / всё свободное место окна (остальные).
        // Box не покидает композицию при переключении — плеер и позиция сохраняются
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .then(
                    when {
                        isFullscreen && controller.supportsOverlayControls -> Modifier.fillMaxSize()
                        isFullscreen -> Modifier.fillMaxWidth().weight(1f)
                        else -> Modifier.heightIn(max = videoMaxHeight).aspectRatio(16f / 9f)
                    }
                )
                .background(androidx.compose.ui.graphics.Color.Black)
                .testTag("video_surface")
        ) {
            val onPlayPause = { if (playerState.isPlaying) controller.pause() else controller.play() }
            val onSeek = { fraction: Float ->
                if (playerState.durationMs > 0) {
                    controller.seekTo((playerState.durationMs * fraction).toLong())
                }
            }
            // V1/DC-5: кастомные контролы мокапа; на Android — слоты media3 Player
            // (center: big-play/replay, bottom: control-bar), нативные контролы не используются
            val overlayControls = controller.supportsOverlayControls && !state.videoError
            NativeVideoSurface(
                controller = controller,
                modifier = Modifier.fillMaxSize(),
                centerControls = if (overlayControls) {
                    {
                        MockupCenterControls(
                            playerState = playerState,
                            onPlayPause = onPlayPause,
                            onReplay = {
                                controller.seekTo(0)
                                controller.play()
                            }
                        )
                    }
                } else null,
                bottomControls = if (overlayControls) {
                    {
                        MockupBottomControls(
                            playerState = playerState,
                            subtitlesOn = state.subtitlesEnabled && hasSubtitles,
                            onPlayPause = onPlayPause,
                            onSeek = onSeek,
                            onToggleCc = onToggleSubtitles,
                            isFullscreen = isFullscreen,
                            onToggleFullscreen = onToggleFullscreen
                        )
                    }
                } else null
            )

            // WASM: DOM-video поверх canvas — Compose-оверлеи показываем только
            // в состояниях, где video-элемент скрыт (до старта / после конца)
            if (!state.videoError && !controller.supportsOverlayControls) {
                if (!playerState.isPlaying && playerState.positionMs == 0L && !playerState.isEnded) {
                    BigPlayOverlay(
                        onPlay = { controller.play() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (playerState.isEnded) {
                    ReplayOverlay(
                        onReplay = {
                            controller.seekTo(0)
                            controller.play()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Субтитры поверх видео в fullscreen (спека §2.3, v1.8): активный cue
            // в нижней части экрана над control-bar. На WASM overlay невидим
            // (DOM-video перекрывает canvas) — рендер безвреден
            if (isFullscreen && state.subtitlesEnabled && !state.videoError) {
                val activeCue = state.subtitleCues.firstOrNull { cue ->
                    playerState.positionMs >= cue.startMs && playerState.positionMs < cue.endMs
                }
                if (activeCue != null) {
                    Text(
                        text = activeCue.text,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 32.dp, end = 32.dp, bottom = 88.dp)
                            .background(Color.Black.copy(alpha = 0.6f), SpeakingShapes.Chip)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("video_subtitle_overlay")
                    )
                }
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

        // WASM/desktop: control-bar ПОД плеером (DOM-video перекрывает canvas, overlay невозможен);
        // остаётся видимым и в fullscreen — там это единственный способ выйти кнопкой
        if (!controller.supportsOverlayControls && !state.videoError) {
            BelowVideoControls(
                playerState = playerState,
                subtitlesOn = state.subtitlesEnabled && hasSubtitles,
                onPlayPause = { if (playerState.isPlaying) controller.pause() else controller.play() },
                onSeek = { fraction ->
                    if (playerState.durationMs > 0) {
                        controller.seekTo((playerState.durationMs * fraction).toLong())
                    }
                },
                onToggleCc = onToggleSubtitles,
                isFullscreen = isFullscreen,
                onToggleFullscreen = onToggleFullscreen
            )
        }

        // В fullscreen транскрипт, CTA и hint скрываются — только видео
        if (!isFullscreen) {
        // Полный транскрипт видео с пословной подсветкой (скроллится внутри панели)
        if (state.subtitlesEnabled && state.subtitleCues.isNotEmpty()) {
            TranscriptPanel(
                cues = state.subtitleCues,
                positionMs = playerState.positionMs,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

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
        } // !isFullscreen
        }
    }
}

/**
 * Центральный слот контролов плеера по мокапу frame-video:
 * big-play 64dp (fade+scale tweenFast, скрывается при воспроизведении),
 * «Начать заново» после окончания (STATE_ENDED).
 * На Android размещается в centerControls-слоте media3 Player.
 */
@Composable
private fun MockupCenterControls(
    playerState: VideoPlayerState,
    onPlayPause: () -> Unit,
    onReplay: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        // «Начать заново» после окончания воспроизведения (STATE_ENDED)
        if (playerState.isEnded) {
            ReplayOverlay(onReplay = onReplay)
        }

        // Big-play (mockups .big-play): появление/исчезание fade+scale tweenFast
        androidx.compose.animation.AnimatedVisibility(
            visible = !playerState.isPlaying && !playerState.isEnded,
            enter = androidx.compose.animation.fadeIn(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()) +
                androidx.compose.animation.scaleIn(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()),
            exit = androidx.compose.animation.fadeOut(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast()) +
                androidx.compose.animation.scaleOut(com.sotospeak.designsystem.theme.SpeakingMotion.tweenFast())
        ) {
            BigPlayOverlay(onPlay = onPlayPause)
        }
    }
}

/**
 * Нижний слот контролов (mockups .video-controls): градиентная подложка +
 * control-bar (play/pause, seek, время, CC, fullscreen).
 * На Android размещается в bottomControls-слоте media3 Player.
 */
@Composable
private fun MockupBottomControls(
    playerState: VideoPlayerState,
    subtitlesOn: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleCc: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                )
            )
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 8.dp)
            .testTag("video_control_bar")
    ) {
        ControlBarContent(
            playerState = playerState,
            subtitlesOn = subtitlesOn,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onToggleCc = onToggleCc,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            contentColor = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Big-play 64dp по центру (мокап .big-play): круг, белый 92%, тень. */
@Composable
private fun BigPlayOverlay(onPlay: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onPlay,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 6.dp,
        modifier = modifier
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

/** «Начать заново» после окончания воспроизведения (мокап replay). */
@Composable
private fun ReplayOverlay(onReplay: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onReplay,
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

/**
 * Control-bar ПОД плеером для платформ без in-canvas видео (WASM: DOM-video
 * поверх canvas). Те же элементы и testTag'и, что у overlay control-bar,
 * но на светлом фоне приложения.
 */
@Composable
private fun BelowVideoControls(
    playerState: VideoPlayerState,
    subtitlesOn: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleCc: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("video_control_bar")
    ) {
        ControlBarContent(
            playerState = playerState,
            subtitlesOn = subtitlesOn,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onToggleCc = onToggleCc,
            isFullscreen = isFullscreen,
            onToggleFullscreen = onToggleFullscreen,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Общее содержимое control-bar: play/pause, seek, время, CC, fullscreen. */
@Composable
private fun ControlBarContent(
    playerState: VideoPlayerState,
    subtitlesOn: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleCc: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
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
            tint = contentColor,
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
        modifier = modifier
            .height(20.dp)
            .testTag("vc_seek"),
        colors = SliderDefaults.colors(
            thumbColor = contentColor,
            activeTrackColor = contentColor,
            inactiveTrackColor = contentColor.copy(alpha = 0.3f)
        )
    )
    Text(
        "${formatTimer((playerState.positionMs / 1000).toInt())} / ${formatTimer((playerState.durationMs / 1000).toInt())}",
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.testTag("vc_time")
    )
    // CC (mockups .vc-btn.cc.on — подсветка #FFD666 снизу)
    Surface(
        onClick = onToggleCc,
        shape = SpeakingShapes.Chip,
        color = if (subtitlesOn) contentColor.copy(alpha = 0.12f) else Color.Transparent,
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
                color = contentColor,
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
    // Fullscreen (спека §2.3, v1.7): на Android — ландшафт + immersive
    IconButton(
        onClick = onToggleFullscreen,
        modifier = Modifier
            .size(48.dp)   // аудит: touch target 48
            .testTag("vc_fullscreen")
    ) {
        Icon(
            imageVector = if (isFullscreen) SpeakingIcons.FullscreenExit else SpeakingIcons.Fullscreen,
            contentDescription = if (isFullscreen) "Свернуть видео" else "На весь экран",
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
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
    libraryTitle: String,
    onNavigateToQuestions: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: com.sotospeak.app.viewmodel.VideoViewModel = org.koin.compose.viewmodel.koinViewModel()
    val state by vm.state.collectAsState()

    // Медиа-HTTP-клиент (Koin single named "media") — единый Ktor-стек стриминга (bd 4d1)
    val mediaClient: io.ktor.client.HttpClient = org.koin.compose.koinInject(org.koin.core.qualifier.named("media"))
    val controller = remember { VideoPlayerController(mediaClient) }
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
        onBack = { vm.onAction(com.sotospeak.app.viewmodel.VideoAction.OnBack) },
        libraryTitle = libraryTitle
    )
}
