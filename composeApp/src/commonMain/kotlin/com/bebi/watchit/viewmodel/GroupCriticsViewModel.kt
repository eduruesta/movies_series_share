package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.models.CriticsResponse
import com.bebi.watchit.data.repository.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupCriticsViewModel(
    private val groupsRepository: GroupsRepository,
    private val currentUsername: String,
    private val groupId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupCriticsUiState())
    val uiState: StateFlow<GroupCriticsUiState> = _uiState.asStateFlow()

    init {
        loadGroupCritics()
    }

    fun loadGroupCritics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            groupsRepository.getGroupCritics(groupId).fold(
                onSuccess = { critics ->
                    _uiState.update { 
                        it.copy(
                            critics = critics,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar las críticas del grupo"
                        )
                    }
                }
            )
        }
    }

    fun createCritic(title: String, review: String, score: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            groupsRepository.createGroupCritic(
                groupId = groupId,
                title = title,
                review = review,
                score = score,
                author = currentUsername
            ).fold(
                onSuccess = { newCritic ->
                    val updatedCritics = _uiState.value.critics + newCritic
                    _uiState.update { 
                        it.copy(
                            critics = updatedCritics,
                            isSubmitting = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isSubmitting = false,
                            error = exception.message ?: "Error al crear la crítica"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class GroupCriticsUiState(
    val critics: List<CriticsResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
)
