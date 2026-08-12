package com.sotospeak.app.player

import androidx.compose.runtime.Composable

/**
 * Платформенные эффекты полноэкранного режима видео (спека Part 2 §3.2, v1.7).
 * Android: ландшафтная ориентация + immersive (скрытие system bars);
 * остальные платформы — no-op (fullscreen там чисто layout'ный).
 * При выходе из fullscreen и размонтировании состояние окна восстанавливается.
 */
@Composable
expect fun VideoFullscreenEffect(enabled: Boolean)
