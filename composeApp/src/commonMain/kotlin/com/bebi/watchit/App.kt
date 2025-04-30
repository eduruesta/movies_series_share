package com.bebi.watchit

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.watchit.theme.AppTheme
import com.bebi.watchit.ui.screens.MediaListScreen

@Composable
internal fun App() = AppTheme {
    // Initialize Koin for iOS (Android is initialized in Application class)

    // Use KoinContext instead of KoinApplication to use the already initialized Koin instance
    Navigator(MediaListScreen()) { navigator ->
        SlideTransition(navigator)
    }

}
