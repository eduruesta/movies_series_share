package com.bebi.app.data

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual val myLang: String?
    get() = NSLocale.currentLocale.languageCode