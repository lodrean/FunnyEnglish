package com.sotospeak.app.accessibility

import java.awt.Toolkit

/** Desktop — системный beep AWT. */
actual fun playTimerWarningSound() {
    Toolkit.getDefaultToolkit().beep()
}
