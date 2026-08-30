package com.sotospeak.app.accessibility

/**
 * Звуковой сигнал последних секунд таймера записи
 * (PROJECT-REVIEW-2026-08-28 §3.1 Д2: «звук/вибро последних 5с»).
 *
 * Вибрация реализована кроссплатформенно через LocalHapticFeedback
 * в [com.sotospeak.app.components.SpeakingTimerRing]; этот expect — только звук.
 */
expect fun playTimerWarningSound()
