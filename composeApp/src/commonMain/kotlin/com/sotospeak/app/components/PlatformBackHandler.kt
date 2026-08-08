package com.sotospeak.app.components

import androidx.compose.runtime.Composable

/**
 * Платформенная обработка системной кнопки/жеста «назад».
 * По мокапу аппбары без стрелки — назад только системный; на Android это
 * androidx.activity BackHandler, на остальных платформах — no-op
 * (desktop/wasm: навигация мышью/жестами браузера не переопределяется).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
