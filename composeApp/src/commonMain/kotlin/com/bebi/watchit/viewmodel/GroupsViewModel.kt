package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.data.repository.GroupsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val groupsRepository: GroupsRepository,
    private val currentUsername: String // Nombre del usuario actual
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            groupsRepository.getGroups().fold(
                onSuccess = { groups ->
                    _uiState.update { 
                        it.copy(
                            groups = groups,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar los grupos"
                        )
                    }
                }
            )
        }
    }

    fun createGroup(name: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            groupsRepository.createGroup(name, description, currentUsername).fold(
                onSuccess = { newGroup ->
                    val updatedGroups = _uiState.value.groups + newGroup
                    _uiState.update { 
                        it.copy(
                            groups = updatedGroups,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al crear el grupo"
                        )
                    }
                }
            )
        }
    }

    fun joinGroup(inviteCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            groupsRepository.joinGroup(inviteCode, currentUsername).fold(
                onSuccess = { updatedGroup ->
                    // Actualizar la lista de grupos con el grupo actualizado
                    val updatedGroups = _uiState.value.groups.map { 
                        if (it.id == updatedGroup.id) updatedGroup else it 
                    }
                    
                    // Si el grupo no estaba en la lista, agregarlo
                    val finalGroups = if (updatedGroups.any { it.id == updatedGroup.id }) {
                        updatedGroups
                    } else {
                        updatedGroups + updatedGroup
                    }
                    
                    _uiState.update { 
                        it.copy(
                            groups = finalGroups,
                            isLoading = false
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al unirse al grupo"
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

data class GroupsUiState(
    val groups: List<GroupResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
