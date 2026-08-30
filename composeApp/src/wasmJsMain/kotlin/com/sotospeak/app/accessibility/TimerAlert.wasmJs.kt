package com.sotospeak.app.accessibility

/**
 * WASM — no-op: Web Audio не подключён в проекте, а без предшествующего жеста
 * пользователя браузер всё равно заблокирует звук (autoplay policy).
 * A11y-анонсы остатка времени работают через live-region (DOM) без звука.
 */
actual fun playTimerWarningSound() = Unit
