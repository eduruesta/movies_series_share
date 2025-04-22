package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.MediaOpinionRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing media opinions
 */
class MediaOpinionViewModel(
    private val repository: MediaOpinionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MediaOpinionUiState())
    val uiState: StateFlow<MediaOpinionUiState> = _uiState.asStateFlow()
    
    init {
        loadOpinions()
    }
    
    private fun loadOpinions() {
        viewModelScope.launch {
            // Set loading state to true
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                repository.getAllOpinions().collect { opinions ->
                    _uiState.update { it.copy(opinions = opinions, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    fun saveOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            try {
                repository.saveOpinion(opinion)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun deleteOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            try {
                repository.deleteOpinion(opinion)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Envía una calificación para una película o serie
     * 
     * @param opinion La opinión a actualizar
     * @param rating La nueva calificación (de 0 a 10)
     */
    fun submitRating(opinion: MediaOpinion, rating: Int) {
        viewModelScope.launch {
            try {
                // Calcular el nuevo promedio y contador de calificaciones
                val newRatingCount = opinion.ratingCount + 1
                
                // Si es la primera calificación, el promedio es igual a la calificación
                // Si no, calculamos el promedio ponderado
                val totalRatingPoints = opinion.averageRating * opinion.ratingCount + rating
                val newAverageRating = totalRatingPoints / newRatingCount
                
                val updatedOpinion = opinion.copy(
                    // La calificación original se mantiene (es la del creador)
                    // pero actualizamos los campos de promedio y contador
                    ratingCount = newRatingCount,
                    averageRating = newAverageRating
                )
                
                repository.saveOpinion(updatedOpinion)
                
                // También podríamos actualizar el UI state directamente si queremos
                // que la UI reaccione inmediatamente sin esperar a que el flow se actualice
                _uiState.update { currentState ->
                    val updatedOpinions = currentState.opinions.map { 
                        if (it.id == opinion.id) updatedOpinion else it 
                    }
                    currentState.copy(opinions = updatedOpinions)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

/**
 * UI state for media opinions
 */
data class MediaOpinionUiState(
    val opinions: List<MediaOpinion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
