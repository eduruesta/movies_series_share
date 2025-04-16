package com.bebi.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bebi.app.di.appModule
import com.bebi.app.theme.AppTheme
import com.bebi.app.ui.screens.MediaListScreen
import org.koin.compose.KoinApplication

@Composable
internal fun App() = AppTheme {
    KoinApplication(application = {
        modules(appModule)
    }) {
        Navigator(MediaListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
