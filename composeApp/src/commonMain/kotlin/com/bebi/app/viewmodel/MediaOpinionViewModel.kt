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
        viewModelScope.launch {
            repository.getAllOpinions().collect { opinions ->
                _uiState.update { it.copy(opinions = opinions) }
            }
        }
    }
    
    fun saveOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            repository.saveOpinion(opinion)
        }
    }
    
    fun deleteOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            repository.deleteOpinion(opinion)
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
