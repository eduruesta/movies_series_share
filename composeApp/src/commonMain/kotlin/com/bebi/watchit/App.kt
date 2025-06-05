package com.bebi.watchit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.watchit.theme.AppTheme
import com.bebi.watchit.ui.screens.HomeScreen

val currentDeepLink: MutableState<String?> = mutableStateOf(null)

@Composable
internal fun App() = AppTheme {
    Navigator(HomeScreen()) { navigator ->
        LaunchedEffect(currentDeepLink.value) {
            val deepLink = currentDeepLink.value
            if (!deepLink.isNullOrEmpty()) {
                DeepLinkHandler.handleDeepLink(deepLink, navigator)
                currentDeepLink.value = null
            }
        }
        
        SlideTransition(navigator)
    }
}
