package com.bebi.watchit

import androidx.compose.ui.window.ComposeUIViewController
import com.bebi.watchit.App
import com.bebi.watchit.di.module.initKoin
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.initialize

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}

fun initialise() {
    Firebase.initialize()
    Firebase.analytics.setAnalyticsCollectionEnabled(true)
    Firebase.database.setLoggingEnabled(true)
    Firebase.database.setPersistenceEnabled(true)
}

/**
 * Procesa un deep link recibido desde iOS.
 * Esta función es llamada desde Swift cuando se recibe un deep link.
 *
 * @param deepLink La URL completa del deep link recibido
 */
fun processDeepLink(deepLink: String) {
    println("Kotlin iOS recibió deep link: $deepLink")
    
    // Actualizamos la variable global con el deep link recibido
    // Esto disparará el LaunchedEffect en App.kt que procesará el deep link
    currentDeepLink.value = deepLink
    println("currentDeepLink actualizado a: ${currentDeepLink.value}")
}