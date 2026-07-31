package com.funnyenglish.app.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Нативная поверхность видеоплеера (спека Part 2 §3.2).
 * Android — PlayerView (Media3), остальные платформы — заглушка.
 */
@Composable
expect fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier = Modifier
)
