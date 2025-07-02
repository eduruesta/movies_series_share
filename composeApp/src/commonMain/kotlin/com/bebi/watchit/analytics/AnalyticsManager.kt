package com.bebi.watchit.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

/**
 * Clase para manejar todos los eventos de analytics de la aplicación
 */
object AnalyticsManager {
    
    // Constantes para eventos comunes
    private const val SCREEN_VIEW = "screen_view"
    private const val SCREEN_NAME = "screen_name"
    private const val SEARCH = "search"
    private const val ITEM_VIEW = "item_view"
    private const val ITEM_CLICK = "item_click"
    private const val OPINION_CREATE = "opinion_create"
    private const val OPINION_SHARE = "opinion_share"
    private const val RATING = "rating"
    

    /**
     * Trackea la vista de una pantalla
     */
    @Composable
    fun trackScreenView(firebaseAnalytics: FirebaseAnalytics, screenName: String) {
        LaunchedEffect(screenName) {
            firebaseAnalytics.logEvent(SCREEN_VIEW) {
                param(SCREEN_NAME, screenName)
            }
        }
    }
    
    /**
     * Trackea una búsqueda de película o serie
     */
    fun trackMediaSearch(firebaseAnalytics: FirebaseAnalytics, query: String, resultsCount: Int) {
        firebaseAnalytics.logEvent(SEARCH) {
            param("search_term", query)
            param("results_count", resultsCount.toString())
        }
    }
    
    /**
     * Trackea la visualización de detalles de una película o serie
     */
    fun trackMediaView(firebaseAnalytics: FirebaseAnalytics, mediaId: String, mediaTitle: String, mediaType: String) {
        firebaseAnalytics.logEvent(ITEM_VIEW) {
            param("media_id", mediaId)
            param("media_title", mediaTitle)
            param("media_type", mediaType)
        }
    }
    
    /**
     * Trackea un click en un elemento de la UI
     */
    fun trackUiElementClick(firebaseAnalytics: FirebaseAnalytics, elementName: String, screenName: String) {
        firebaseAnalytics.logEvent(ITEM_CLICK) {
            param("element_name", elementName)
            param("screen_name", screenName)
        }
    }
    
    /**
     * Trackea la creación de una opinión
     */
    fun trackOpinionCreation(firebaseAnalytics: FirebaseAnalytics, mediaTitle: String, rating: Float, hasComment: Boolean) {
        firebaseAnalytics.logEvent(OPINION_CREATE) {
            param("media_title", mediaTitle)
            param("rating", rating.toString())
            param("has_comment", hasComment.toString())
        }
    }
    
    /**
     * Trackea cuando un usuario comparte una opinión
     */
    fun trackShareOpinion(firebaseAnalytics: FirebaseAnalytics, mediaId: String, mediaTitle: String, shareType: String) {
        firebaseAnalytics.logEvent(OPINION_SHARE) {
            param("media_id", mediaId)
            param("media_title", mediaTitle)
            param("share_type", shareType)
        }
    }
    
    /**
     * Trackea cuando un usuario puntúa una película o serie
     */
    fun trackRating(firebaseAnalytics: FirebaseAnalytics, mediaTitle: String, rating: Float) {
        firebaseAnalytics.logEvent(RATING) {
            param("media_title", mediaTitle)
            param("rating_value", rating.toString())
        }
    }
    
    /**
     * Trackea cuando el usuario marca como favorito
     */
    fun trackAddToFavorites(firebaseAnalytics: FirebaseAnalytics, mediaId: String, mediaTitle: String) {
        firebaseAnalytics.logEvent("add_to_favorites") {
            param("media_id", mediaId)
            param("media_title", mediaTitle)
        }
    }
    
    /**
     * Trackea cuando el usuario desmarca como favorito
     */
    fun trackRemoveFromFavorites(firebaseAnalytics: FirebaseAnalytics, mediaId: String, mediaTitle: String) {
        firebaseAnalytics.logEvent("remove_from_favorites") {
            param("media_id", mediaId)
            param("media_title", mediaTitle)
        }
    }
    
    /**
     * Trackea cuando hay un error en la aplicación
     */
    fun trackError(firebaseAnalytics: FirebaseAnalytics, errorType: String, errorMessage: String, screenName: String) {
        firebaseAnalytics.logEvent("app_error") {
            param("error_type", errorType)
            param("error_message", errorMessage)
            param("screen_name", screenName)
        }
    }
    
    /**
     * Trackea cuando se añade una película/serie a una lista
     */
    fun trackAddToList(firebaseAnalytics: FirebaseAnalytics, mediaId: String, mediaTitle: String, listName: String) {
        firebaseAnalytics.logEvent("add_to_list") {
            param("media_id", mediaId)
            param("media_title", mediaTitle)
            param("list_name", listName)
        }
    }
    
    /**
     * Trackea el inicio de sesión exitoso
     */
    fun trackLoginSuccess(firebaseAnalytics: FirebaseAnalytics, method: String) {
        firebaseAnalytics.logEvent("login") {
            param("login_method", method)
            param("status", "success")
        }
    }
    
    /**
     * Trackea un error de inicio de sesión
     */
    fun trackLoginError(firebaseAnalytics: FirebaseAnalytics, method: String, errorMessage: String) {
        firebaseAnalytics.logEvent("login_error") {
            param("login_method", method)
            param("error_message", errorMessage)
        }
    }
    
    /**
     * Trackea cuando un usuario filtra contenido
     */
    fun trackFilterContent(firebaseAnalytics: FirebaseAnalytics, filterType: String, filterValue: String) {
        firebaseAnalytics.logEvent("filter_content") {
            param("filter_type", filterType)
            param("filter_value", filterValue)
        }
    }
}
