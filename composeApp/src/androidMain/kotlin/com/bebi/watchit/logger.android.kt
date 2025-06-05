package com.bebi.watchit

actual object KLogger {
    actual fun d(message: String) {
        android.util.Log.d("KMM", message)
    }

    actual fun e(message: String) {
        android.util.Log.e("KMM", message)
    }
}