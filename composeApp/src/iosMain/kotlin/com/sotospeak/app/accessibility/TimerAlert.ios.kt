package com.sotospeak.app.accessibility

import platform.AudioToolbox.AudioServicesPlaySystemSound

/** Системный звук 1057 («Tock») — короткий тик последних секунд таймера. */
actual fun playTimerWarningSound() {
    AudioServicesPlaySystemSound(1057u)
}
