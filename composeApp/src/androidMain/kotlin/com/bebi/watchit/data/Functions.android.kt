package com.bebi.watchit.data

import java.util.Locale

actual val myLang: String?
    get() = Locale.getDefault().language

actual val myCountry: String?
    get() = Locale.getDefault().country

actual typealias JavaSerializable = java.io.Serializable