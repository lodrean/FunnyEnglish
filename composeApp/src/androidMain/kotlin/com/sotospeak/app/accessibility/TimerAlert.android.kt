package com.sotospeak.app.accessibility

import android.media.AudioManager
import android.media.ToneGenerator

/** Ленивый синглтон: ToneGenerator держит аудио-ресурс, пересоздавать на каждый тик не нужно. */
private var toneGenerator: ToneGenerator? = null

actual fun playTimerWarningSound() {
    val generator = toneGenerator
        ?: ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70).also { toneGenerator = it }
    generator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
}
