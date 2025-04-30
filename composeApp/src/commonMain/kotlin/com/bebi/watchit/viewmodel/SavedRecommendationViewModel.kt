package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.repository.SavedRecommendationRepository
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.model.SavedRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las recomendaciones guardadas
 */
class SavedRecommendationViewModel(
    private val repository: SavedRecommendationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    init {
        loadSavedRecommendations()
    }

    /**
     * Carga las recomendaciones guardadas
     */
    fun loadSavedRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Usamos una colección finita en lugar de una continua
                val savedRecommendations = repository.getAllSavedRecommendations().first()

                _uiState.update { currentState ->
                    val currentQuery = currentState.searchQuery
                    val filtered = if (currentQuery.isNotEmpty()) {
                        filterRecommendations(savedRecommendations, currentQuery)
                    } else {
                        savedRecommendations
                    }

                    currentState.copy(
                        savedRecommendations = savedRecommendations,
                        filteredRecommendations = filtered,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Verifica si una recomendación está guardada
     */
    fun isRecommendationSaved(opinionId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isSaved = repository.isRecommendationSaved(opinionId)
                callback(isSaved)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    /**
     * Guarda una recomendación
     */
    fun saveRecommendation(opinion: MediaOpinion, callback: (RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            try {
                if (repository.isRecommendationSaved(opinion.id)) {
                    callback(RecommendationMessage.AlreadySaved(opinion.title))
                    return@launch
                }

                val result = repository.saveRecommendation(opinion)
                if (result) {
                    callback(RecommendationMessage.Saved(opinion.title))
                } else {
                    callback(RecommendationMessage.ErrorSaving("Unknown error"))
                }
            } catch (e: Exception) {
                callback(RecommendationMessage.ErrorSaving(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Elimina una recomendación guardada
     */
    fun removeRecommendation(
        recommendation: SavedRecommendation,
        callback: (RecommendationMessage) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.removeSavedRecommendation(recommendation.opinionId)
                if (result) {
                    val updatedRecommendations = repository.getAllSavedRecommendations().first()

                    _uiState.update { it.copy(savedRecommendations = updatedRecommendations) }
                    callback(RecommendationMessage.Removed(recommendation.title))
                } else {
                    callback(RecommendationMessage.ErrorRemoving("Unknown error"))
                }
            } catch (e: Exception) {
                callback(RecommendationMessage.ErrorRemoving(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Actualiza la consulta de búsqueda y filtra las recomendaciones
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isNotEmpty()) {
                filterRecommendations(currentState.savedRecommendations, query)
            } else {
                currentState.savedRecommendations
            }
            currentState.copy(
                searchQuery = query,
                filteredRecommendations = filtered
            )
        }
    }

    /**
     * Filtra las recomendaciones basándose en la consulta de búsqueda
     */
    private fun filterRecommendations(
        recommendations: List<SavedRecommendation>,
        query: String
    ): List<SavedRecommendation> {
        if (query.isBlank()) return recommendations

        val lowercaseQuery = query.lowercase()
        return recommendations.filter { recommendation ->
            recommendation.title.lowercase().contains(lowercaseQuery) ||
                    recommendation.genre.lowercase().contains(lowercaseQuery)
        }
    }
}

/**
 * Estado de la UI para recomendaciones
 */
data class RecommendationUiState(
    val savedRecommendations: List<SavedRecommendation> = emptyList(),
    val filteredRecommendations: List<SavedRecommendation> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Tipos de mensajes para las recomendaciones
 */
sealed class RecommendationMessage {
    data class Removed(val title: String) : RecommendationMessage()
    data object NotSaved : RecommendationMessage()
    data class ErrorRemoving(val error: String) : RecommendationMessage()
    data class Saved(val title: String) : RecommendationMessage()
    data class AlreadySaved(val title: String) : RecommendationMessage()
    data class ErrorSaving(val error: String) : RecommendationMessage()
}
