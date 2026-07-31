package com.funnyenglish.app.recorder

import kotlinx.coroutines.flow.StateFlow

/** Состояние записи голоса (спека Part 2 §4.1). */
sealed interface VoiceRecorderState {
    data object Idle : VoiceRecorderState
    data object Recording : VoiceRecorderState
    data class Stopped(val filePath: String) : VoiceRecorderState
    /** Микрофон занят, нет места и т.п. — message уже человеческий текст для UI. */
    data class Error(val message: String) : VoiceRecorderState
}

/**
 * Запись голоса для Speaking Trainer (спека Part 2 §4.1, спайк CL-T6).
 *
 * Формат вывода: AAC в контейнере MPEG-4 (.m4a), моно, 44.1 кГц, 96 кбит/с.
 * 30 секунд ≈ 360 КБ — укладываемся в лимит PRD (~1–2 МБ).
 * Файл создаётся в директории [com.funnyenglish.app.storage.RecordingFileStorage].
 */
expect class VoiceRecorder() {
    val state: StateFlow<VoiceRecorderState>

    /** Начать запись в файл "<outputFileName>.m4a" в директории записей. */
    fun start(outputFileName: String)

    /** Корректное завершение записи; возвращает путь к файлу или null при ошибке. */
    fun stop(): String?

    /** Отменить запись: удалить файл, state → Idle. */
    fun cancel()

    /** Освободить ресурсы (recorder, аудиофокус). */
    fun release()
}
