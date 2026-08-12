package com.sotospeak.app.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Нативная поверхность видеоплеера (спека Part 2 §3.2).
 * Android — media3-ui-compose-material3 `Player` (ContentFrame + слоты),
 * остальные платформы — заглушка/DOM-video (слоты игнорируются).
 *
 * [centerControls]/[bottomControls] — кастомные контролы мокапа (big-play/replay
 * и control-bar), на Android размещаются в слотах Player; имеет смысл передавать
 * только когда `controller.supportsOverlayControls == true`.
 */
@Composable
expect fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier = Modifier,
    centerControls: (@Composable () -> Unit)? = null,
    bottomControls: (@Composable () -> Unit)? = null
)
