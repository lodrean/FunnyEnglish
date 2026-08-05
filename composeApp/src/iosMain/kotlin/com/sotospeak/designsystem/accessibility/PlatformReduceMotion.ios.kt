package com.sotospeak.designsystem.accessibility

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun platformReduceMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
