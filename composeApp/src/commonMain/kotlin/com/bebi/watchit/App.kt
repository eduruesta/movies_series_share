package com.bebi.watchit

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.watchit.theme.AppTheme
import com.bebi.watchit.ui.screens.HomeScreen

@Composable
internal fun App() = AppTheme {

    Navigator(HomeScreen()) { navigator ->
        SlideTransition(navigator)
    }

}
