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
    
    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()
    
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
     * Guarda una opinión como recomendación
     */
    fun saveRecommendation(opinion: MediaOpinion, onComplete: (Boolean, RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val result = repository.saveRecommendation(opinion)
                if (result) {
                    // Si se guardó correctamente
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(true, RecommendationMessage.SAVED(opinion.title))
                } else {
                    // Si ya estaba guardada
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(false, RecommendationMessage.ALREADY_SAVED(opinion.title))
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
                onComplete(false, RecommendationMessage.ERROR_SAVING(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Guarda una recomendación basada en IDs
     */
    fun saveRecommendation(title: String, mediaId: String, opinionId: Long, onComplete: (RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val result = repository.saveRecommendation(title, mediaId, opinionId)
                if (result) {
                    // Si se guardó correctamente
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(RecommendationMessage.SAVED(title))
                } else {
                    // Si ya estaba guardada
                    _uiState.update { it.copy(isSaving = false) }
                    onComplete(RecommendationMessage.ALREADY_SAVED(title))
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
                onComplete(RecommendationMessage.ERROR_SAVING(e.message ?: "Error desconocido"))
            }
        }
    }
    
    /**
     * Elimina una recomendación guardada
     */
    fun removeRecommendation(title: String, opinionId: Long, onComplete: (Boolean, RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }
            
            try {
                val result = repository.removeSavedRecommendation(opinionId)
                if (result) {
                    // Si se eliminó correctamente
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(true, RecommendationMessage.REMOVED(title))
                } else {
                    // Si no estaba guardada
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(false, RecommendationMessage.NOT_SAVED)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isRemoving = false,
                        error = e.message
                    )
                }
                onComplete(false, RecommendationMessage.ERROR_REMOVING(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Elimina una recomendación guardada, sobrecarga para trabajar sin boolean
     */
    fun removeRecommendation(title: String, opinionId: Long, onComplete: (RecommendationMessage) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true) }
            
            try {
                val result = repository.removeSavedRecommendation(opinionId)
                if (result) {
                    // Si se eliminó correctamente
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(RecommendationMessage.REMOVED(title))
                } else {
                    // Si no estaba guardada
                    _uiState.update { it.copy(isRemoving = false) }
                    onComplete(RecommendationMessage.NOT_SAVED)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isRemoving = false,
                        error = e.message
                    )
                }
                onComplete(RecommendationMessage.ERROR_REMOVING(e.message ?: "Error desconocido"))
            }
        }
    }
}

/**
 * Estado de la UI para recomendaciones
 */
data class RecommendationUiState(
    val recommendations: List<SavedRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isRemoving: Boolean = false,
    val error: String? = null
)

/**
 * Tipos de mensajes para las recomendaciones
 */
sealed class RecommendationMessage {
    data class REMOVED(val title: String) : RecommendationMessage()
    object NOT_SAVED : RecommendationMessage()
    data class ERROR_REMOVING(val error: String) : RecommendationMessage()
    data class SAVED(val title: String) : RecommendationMessage()
    data class ALREADY_SAVED(val title: String) : RecommendationMessage()
    data class ERROR_SAVING(val error: String) : RecommendationMessage()
}
