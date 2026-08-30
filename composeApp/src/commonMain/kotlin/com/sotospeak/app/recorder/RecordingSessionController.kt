package com.sotospeak.app.recorder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Общая механика записи Training/Practice (bd FunnyEnglish-5tf.5, предложение 1
 * PROJECT-REVIEW-2026-08-28): обратный отсчёт лимита и длительность текущей записи.
 * Раньше таймер и расчёт длительности были скопированы в обеих ViewModel.
 *
 * Контроллер живёт внутри VM (`scope = viewModelScope`), экран про него не знает:
 * экран по-прежнему владеет VoiceRecorder и шлёт OnRecorderStarted/OnRecorderStopped.
 */
class RecordingSessionController(private val scope: CoroutineScope) {

    private var timerJob: Job? = null
    private var startedAtMs: Long? = null

    /** Отметка реального старта записи (экран подтвердил VoiceRecorder.state=Recording). */
    fun markRecordingStarted(nowMs: Long = Clock.System.now().toEpochMilliseconds()) {
        startedAtMs = nowMs
    }

    /** Длительность текущей записи; 0, если старт не был отмечен. */
    fun elapsedMs(nowMs: Long = Clock.System.now().toEpochMilliseconds()): Long =
        startedAtMs?.let { (nowMs - it).coerceAtLeast(0) } ?: 0L

    /**
     * Обратный отсчёт: [onTick] вызывается сразу с [limitSeconds], затем каждую
     * секунду до 0. По достижении 0 VM прекращает тикать — экран видит
     * remainingSeconds == 0 и останавливает VoiceRecorder.
     */
    fun startTimer(limitSeconds: Int, onTick: (remainingSeconds: Int) -> Unit) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var remaining = limitSeconds
            onTick(remaining)
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                onTick(remaining)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /** Полный сброс сессии записи (повторный вход на экран). */
    fun reset() {
        stopTimer()
        startedAtMs = null
    }
}
