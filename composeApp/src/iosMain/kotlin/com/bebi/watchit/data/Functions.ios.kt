package com.bebi.watchit.data

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual val myLang: String?
    get() = NSLocale.currentLocale.languageCode

actual val myCountry: String?
    get() = NSLocale.currentLocale.countryCode

actual interface JavaSerializable