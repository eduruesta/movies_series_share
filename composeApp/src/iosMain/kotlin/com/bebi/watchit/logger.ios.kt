package com.bebi.watchit
import platform.Foundation.NSLog

actual object KLogger {
    actual fun d(message: String) {
        NSLog("🟢 DEBUG: %@", message)
    }

    actual fun e(message: String) {
        NSLog("🔴 ERROR: %@", message)
    }
}