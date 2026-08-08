package com.sotospeak.app.components

import androidx.compose.runtime.Composable

/** Desktop: системной кнопки «назад» нет — no-op. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
