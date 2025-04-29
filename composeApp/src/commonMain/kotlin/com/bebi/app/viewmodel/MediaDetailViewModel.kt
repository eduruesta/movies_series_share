package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.MediaOpinionRepository
import com.bebi.app.data.repository.SavedRecommendationRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Media Detail screen
 */
class MediaDetailViewModel(
    private val repository: MediaOpinionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MediaDetailUiState())
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Loads a specific opinion by ID
     */
    fun loadOpinionById(id: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val opinion = repository.getOpinionByIdDirect(id)
                
                _uiState.update { 
                    it.copy(
                        opinion = opinion,
                        isLoading = false,
                        error = if (opinion == null) MediaDetailError.NOT_FOUND else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = MediaDetailError.GENERIC
                    )
                }
            }
        }
    }

    /**
     * Establece directamente un MediaOpinion para elementos TMDB
     * que no están guardados en la base de datos
     */
    fun setTmdbMediaOpinion(opinion: MediaOpinion) {
        _uiState.update {
            it.copy(
                opinion = opinion,
                isLoading = false,
                error = null
            )
        }
    }
}

/**
 * UI state for the Media Detail screen
 */
data class MediaDetailUiState(
    val opinion: MediaOpinion? = null,
    val isLoading: Boolean = false,
    val error: MediaDetailError? = null
)

/**
 * Tipos de errores para la pantalla de detalle
 * Estos se traducirán a strings en la UI
 */
enum class MediaDetailError {
    NOT_FOUND,
    GENERIC
}
