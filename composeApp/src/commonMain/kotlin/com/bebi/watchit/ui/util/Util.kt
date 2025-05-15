package com.bebi.watchit.ui.util

import kotlin.math.round

/**
 * Función de extensión para formatear un Float con un decimal fijo
 */
fun Float.formatWithOneDecimal(): String {
    val rounded = round(this * 10) / 10
    return if (rounded == rounded.toInt().toFloat()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}