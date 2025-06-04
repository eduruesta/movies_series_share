package com.bebi.watchit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        
        // Procesamos el intent inicial por si contiene un deep link
        handleIntent(intent)
        
        setContent { App() }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Procesamos los nuevos intents recibidos mientras la app está en ejecución
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        // Verificamos si el intent contiene datos de un deep link
        intent?.data?.toString()?.let { deepLinkUri ->
            // Actualizamos la variable global con el deep link recibido
            currentDeepLink.value = deepLinkUri
        }
    }
}

@Preview
@Composable
fun AppPreview() { App() }
