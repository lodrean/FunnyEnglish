package com.funnyenglish.app.recorder

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import com.funnyenglish.app.storage.RecordingFileStorage
import com.funnyenglish.shared.platform.AndroidContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

/** Минимум свободного места для старта записи — 5 МБ (PRD Edge Cases «Мало места», §4.2). */
private const val MIN_FREE_SPACE_BYTES = 5L * 1024 * 1024

/**
 * Android actual: MediaRecorder → AAC/m4a (спека Part 2 §4.2).
 *
 * Аудиофокус (§4.4): при AUDIOFOCUS_LOSS* (звонок и т.п.) запись сама
 * останавливается и выставляет Stopped — VM заметит по state и сохранит черновик.
 */
actual class VoiceRecorder {

    private val context get() = AndroidContextHolder.requireContext()
    private val storage = RecordingFileStorage()

    private val _state = MutableStateFlow<VoiceRecorderState>(VoiceRecorderState.Idle)
    actual val state: StateFlow<VoiceRecorderState> = _state.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null

    private val audioManager: AudioManager
        get() = context.getSystemService(AudioManager::class.java)

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Прерывание (звонок и т.п.) — автостоп, файл сохраняем (НЕ cancel)
                if (_state.value == VoiceRecorderState.Recording) stopInternal()
        }
    }

    /** AudioFocusRequest — API 26+; на 24–25 используем legacy requestAudioFocus. */
    private var focusRequest: AudioFocusRequest? = null

    actual fun start(outputFileName: String) {
        if (storage.usableSpaceBytes() < MIN_FREE_SPACE_BYTES) {
            _state.value = VoiceRecorderState.Error("Недостаточно места на устройстве")
            return
        }

        releaseRecorder()
        val file = File(storage.recordingsDir(), "$outputFileName.m4a")

        val newRecorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(96_000)
            setAudioChannels(1)
            setOutputFile(file.absolutePath)
        }

        try {
            newRecorder.prepare()
        } catch (e: IOException) {
            newRecorder.release()
            file.delete()
            _state.value = VoiceRecorderState.Error("Не удалось начать запись")
            return
        } catch (e: IllegalStateException) {
            newRecorder.release()
            file.delete()
            _state.value = VoiceRecorderState.Error("Не удалось начать запись")
            return
        }

        requestAudioFocus()
        try {
            newRecorder.start()
        } catch (e: RuntimeException) {
            // Микрофон занят другим приложением
            newRecorder.release()
            abandonAudioFocus()
            file.delete()
            _state.value = VoiceRecorderState.Error("Микрофон занят другим приложением")
            return
        }

        recorder = newRecorder
        currentPath = file.absolutePath
        _state.value = VoiceRecorderState.Recording
    }

    actual fun stop(): String? = stopInternal()

    actual fun cancel() {
        releaseRecorder()
        abandonAudioFocus()
        currentPath?.let { File(it).delete() }
        currentPath = null
        _state.value = VoiceRecorderState.Idle
    }

    actual fun release() {
        releaseRecorder()
        abandonAudioFocus()
        _state.value = VoiceRecorderState.Idle
    }

    /** Общий путь остановки: ручной stop() и автостоп по потере аудиофокуса. */
    private fun stopInternal(): String? {
        val activeRecorder = recorder ?: return null
        return try {
            activeRecorder.stop()
            val path = currentPath
            _state.value = if (path != null) {
                VoiceRecorderState.Stopped(path)
            } else {
                VoiceRecorderState.Error("Не удалось завершить запись")
            }
            path
        } catch (e: RuntimeException) {
            // Слишком короткая запись — файл невалиден, удаляем
            currentPath?.let { File(it).delete() }
            _state.value = VoiceRecorderState.Error("Не удалось завершить запись")
            null
        } finally {
            releaseRecorder()
            abandonAudioFocus()
        }
    }

    /** Конструктор с context — API 31+; minSdk 24 → на старых deprecated-вариант. */
    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

    private fun releaseRecorder() {
        try {
            recorder?.release()
        } catch (e: RuntimeException) {
            // release() после неудачного start() — безопасно игнорируем
        }
        recorder = null
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }
}
