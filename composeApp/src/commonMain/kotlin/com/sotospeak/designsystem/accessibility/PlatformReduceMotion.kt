package com.sotospeak.designsystem.accessibility

import androidx.compose.runtime.Composable

/**
 * Платформенное определение Reduce motion (DS-5, tokens.json motion.recPulse:
 * «при Reduce motion — статичный индикатор»).
 *
 * Android: animator duration scale == 0 (настройка разработчика / accessibility).
 * iOS: UIAccessibilityIsReduceMotionEnabled. Desktop: false. WASM: prefers-reduced-motion.
 */
@Composable
expect fun platformReduceMotionEnabled(): Boolean
