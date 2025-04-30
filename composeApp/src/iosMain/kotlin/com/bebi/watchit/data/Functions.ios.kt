package com.bebi.watchit.data

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual val myLang: String?
    get() = NSLocale.currentLocale.languageCode

actual interface JavaSerializable