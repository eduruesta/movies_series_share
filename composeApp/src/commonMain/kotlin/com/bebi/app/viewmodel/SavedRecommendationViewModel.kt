package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.SavedRecommendationRepository
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation
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
                
                _uiState.update { 
                    it.copy(
                        savedRecommendations = savedRecommendations,
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
                // Verificar si ya está guardada
                if (repository.isRecommendationSaved(opinion.id)) {
                    callback(RecommendationMessage.AlreadySaved(opinion.title))
                    return@launch
                }

                // Guardar la recomendación
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
    fun removeRecommendation(recommendation: SavedRecommendation, callback: (RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.removeSavedRecommendation(recommendation.opinionId)
                if (result) {
                    // Recargar las recomendaciones usando una colección finita
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
}

/**
 * Estado de la UI para recomendaciones
 */
data class RecommendationUiState(
    val savedRecommendations: List<SavedRecommendation> = emptyList(),
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
