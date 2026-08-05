package com.sotospeak.app.player

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Стаб видеоплеера: воспроизведение недоступно на этой платформе (спека Part 2 §3.2).
 */
actual class VideoPlayerController {
    private val _state = MutableStateFlow(VideoPlayerState(error = "unsupported"))
    actual val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    actual fun prepare(url: String) = Unit
    actual fun play() = Unit
    actual fun pause() = Unit
    actual fun seekTo(positionMs: Long) = Unit
    actual fun release() = Unit
}

@Composable
actual fun NativeVideoSurface(
    controller: VideoPlayerController,
    modifier: Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "Видео недоступно на этой платформе",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
