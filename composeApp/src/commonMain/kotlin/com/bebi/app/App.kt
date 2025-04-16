package com.bebi.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.app.theme.AppTheme
import com.bebi.app.ui.screens.MediaListScreen

@Composable
internal fun App() = AppTheme {
    Navigator(MediaListScreen()) { navigator ->
        SlideTransition(navigator)
    }
}

// Removed sealed class Screen as it is no longer used
