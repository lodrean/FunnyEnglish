package com.sotospeak.designsystem.accessibility

import androidx.compose.runtime.Composable

/** Desktop: системный reduce-motion не читаем, анимации включены */
@Composable
actual fun platformReduceMotionEnabled(): Boolean = false
