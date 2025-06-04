package com.bebi.watchit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.watchit.theme.AppTheme
import com.bebi.watchit.ui.screens.HomeScreen

// Variable global para almacenar el deep link recibido
// Se actualiza desde Android (AppActivity) e iOS (main.kt)
val currentDeepLink: MutableState<String?> = mutableStateOf(null)

@Composable
internal fun App() = AppTheme {
    // Creamos el Navigator directamente como un Composable
    Navigator(HomeScreen()) { navigator ->
        // Este LaunchedEffect procesa los deep links cuando cambia currentDeepLink
        LaunchedEffect(currentDeepLink.value) {
            val deepLink = currentDeepLink.value
            if (!deepLink.isNullOrEmpty()) {
                println("Procesando deep link en App.kt: $deepLink")
                DeepLinkHandler.handleDeepLink(deepLink, navigator)
                currentDeepLink.value = null
                println("currentDeepLink reseteado a null")
            }
        }
        
        SlideTransition(navigator)
    }
}
