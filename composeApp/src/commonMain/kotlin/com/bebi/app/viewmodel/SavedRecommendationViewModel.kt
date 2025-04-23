package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.SavedRecommendationRepository
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las recomendaciones guardadas
 */
class SavedRecommendationViewModel(
    private val repository: SavedRecommendationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavedRecommendationUiState())
    val uiState: StateFlow<SavedRecommendationUiState> = _uiState.asStateFlow()
    
    init {
        loadSavedRecommendations()
    }
    
    /**
     * Carga todas las recomendaciones guardadas
     */
    fun loadSavedRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                repository.getAllSavedRecommendations().collect { recommendations ->
                    _uiState.update { 
                        it.copy(
                            recommendations = recommendations,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    /**
     * Guarda una opinión como recomendación
     * @return Flow<Boolean> que emite true si se guardó correctamente, false si ya estaba guardada
     */
    fun saveRecommendation(opinion: MediaOpinion, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val result = repository.saveRecommendation(opinion)
                
                if (result) {
                    // Si se guardó correctamente
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(true, "${opinion.title} fue guardada en tus Recomendaciones")
                } else {
                    // Si ya estaba guardada
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(false, "${opinion.title} ya estaba en tus Recomendaciones")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
                onComplete(false, "Error al guardar: ${e.message}")
            }
        }
    }
    
    /**
     * Verifica si una opinión está guardada
     */
    fun isRecommendationSaved(opinionId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isSaved = repository.isRecommendationSaved(opinionId)
                onResult(isSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                onResult(false)
            }
        }
    }
    
    /**
     * Elimina una recomendación guardada
     */
    fun removeRecommendation(opinionId: Long, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }
            
            try {
                val result = repository.removeSavedRecommendation(opinionId)
                
                if (result) {
                    // Si se eliminó correctamente
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(true, "Eliminado de tus Recomendaciones")
                } else {
                    // Si no estaba guardada
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(false, "No estaba en tus Recomendaciones")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isRemoving = false,
                        error = e.message
                    )
                }
                onComplete(false, "Error al eliminar: ${e.message}")
            }
        }
    }
}

/**
 * Estado de la UI para las recomendaciones guardadas
 */
data class SavedRecommendationUiState(
    val recommendations: List<SavedRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isRemoving: Boolean = false,
    val error: String? = null
)
