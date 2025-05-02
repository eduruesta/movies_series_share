package com.bebi.watchit.data.domain

import org.koin.compose.koinInject

enum class Language(val iso: String) {
    English(iso = "en"),
    Spanish(iso = "es")
}