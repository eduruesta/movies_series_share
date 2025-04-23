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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val opinion = repository.getOpinionByIdDirect(id)
                
                _uiState.update { 
                    it.copy(
                        opinion = opinion,
                        isLoading = false,
                        error = if (opinion == null) "No se encontró la opinión" else null
                    ) 
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
}

/**
 * UI state for the Media Detail screen
 */
data class MediaDetailUiState(
    val opinion: MediaOpinion? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
