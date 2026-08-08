package com.sotospeak.app.components

import androidx.compose.runtime.Composable

/** iOS: жест «назад» обрабатывается навигацией платформы — no-op. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
