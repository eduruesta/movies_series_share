package com.bebi.watchit.data

import java.util.Locale

actual val myLang: String?
    get() = Locale.getDefault().language

actual typealias JavaSerializable = java.io.Serializable