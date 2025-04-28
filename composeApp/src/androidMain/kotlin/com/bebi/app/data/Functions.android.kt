package com.bebi.app.data

import java.util.Locale

actual val myLang: String?
    get() = Locale.getDefault().language