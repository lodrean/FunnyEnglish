package com.sotospeak.app.tests

import com.sotospeak.app.viewmodel.TrainingViewModel
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Юнит-тесты лимитов попыток Training (спека Part 2 §5.3, §10.1):
 * попытка 1 → 80с, 2 → 50с, 3 → 30с.
 */
class TrainingTimerTest {

    @Test
    fun attemptLimitsAre80_50_30() {
        assertEquals(80, TrainingViewModel.timerLimitFor(1))
        assertEquals(50, TrainingViewModel.timerLimitFor(2))
        assertEquals(30, TrainingViewModel.timerLimitFor(3))
    }

    @Test
    fun outOfRangeAttemptsFallBackTo30() {
        // Защита: любой номер ≥3 — лимит 30 (максимум 3 попытки)
        assertEquals(30, TrainingViewModel.timerLimitFor(4))
        assertEquals(30, TrainingViewModel.timerLimitFor(0))
    }

    @Test
    fun maxAttemptsIs3() {
        assertEquals(3, TrainingViewModel.MAX_ATTEMPTS)
    }
}
