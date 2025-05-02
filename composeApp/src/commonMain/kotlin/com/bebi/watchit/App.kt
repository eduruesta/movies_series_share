package com.bebi.watchit

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.watchit.theme.AppTheme
import com.bebi.watchit.ui.screens.MediaListScreen

@Composable
internal fun App() = AppTheme {

    Navigator(MediaListScreen()) { navigator ->
        SlideTransition(navigator)
    }

}
