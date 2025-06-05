package com.bebi.watchit

import cafe.adriel.voyager.navigator.Navigator
import com.bebi.watchit.ui.screens.GroupsScreen

/**
 * Clase para manejar deep links en la aplicación.
 * Esta clase centraliza el procesamiento de enlaces profundos
 * para redirigir al usuario a la pantalla correcta.
 */
class DeepLinkHandler {
    companion object {
        // Constantes para los esquemas y hosts de deep link
        const val SCHEME = "criticly"
        const val HOST_GROUP_JOIN = "group.join"
        
        // Procesa un deep link y navega a la pantalla correspondiente
        fun handleDeepLink(deepLink: String, navigator: Navigator) {
            val inviteCode = extractInviteCodeFromDeepLink(deepLink)

            if (!inviteCode.isNullOrEmpty()) {

                // En lugar de hacer push directamente, verificamos si ya existe un GroupsScreen
                // Esto es más importante para iOS, donde parece que la navegación no funciona correctamente con deep links
                val isGroupScreenAlreadyInStack = navigator.items.any { it is GroupsScreen }
                
                if (isGroupScreenAlreadyInStack) {
                    println("DeepLinkHandler: GroupsScreen ya está en el stack, reemplazándola")
                    // Si ya existe, necesitamos encontrarla y reemplazarla
                    val indexOfGroupScreen = navigator.items.indexOfFirst { it is GroupsScreen }
                    if (indexOfGroupScreen >= 0) {
                        // Reemplazar la pantalla existente con una nueva instancia que tenga el código
                        navigator.replaceAll(navigator.items.toMutableList().apply { 
                            this[indexOfGroupScreen] = GroupsScreen(inviteCode)
                        })
                    }
                } else {
                    println("DeepLinkHandler: Haciendo push de una nueva GroupsScreen")
                    navigator.push(GroupsScreen(inviteCode))
                }
            } else {
                println("DeepLinkHandler: código de invitación nulo o vacío, no se realizará navegación")
            }
        }

        // Obtiene el código de invitación desde una URL de deep link
        private fun extractInviteCodeFromDeepLink(deepLink: String?): String? {
            if (deepLink.isNullOrEmpty()) return null
            
            try {
                // Comprueba si es un deep link válido para unirse a un grupo
                val uri = deepLink.trim()
                if (!uri.startsWith("$SCHEME://$HOST_GROUP_JOIN/")) return null
                
                // Extrae el código de invitación
                return uri.substringAfterLast("/")
            } catch (e: Exception) {
                println("DeepLinkHandler: Error al extraer código de invitación: ${e.message}")
                return null
            }
        }
    }
}
