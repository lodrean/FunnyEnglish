package com.sotospeak.designsystem.accessibility

import androidx.compose.runtime.Composable
import kotlinx.browser.window

@Composable
actual fun platformReduceMotionEnabled(): Boolean =
    window.matchMedia("(prefers-reduced-motion: reduce)").matches
