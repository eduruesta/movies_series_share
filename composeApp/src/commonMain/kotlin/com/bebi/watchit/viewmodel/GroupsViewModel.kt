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
    private val currentUserId: String,
    private val userName: String = "Usuario",
    private val userEmail: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()
    
    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            groupsRepository.getGroupsByMemberId(currentUserId).fold(
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

            groupsRepository.createGroup(name, description, currentUserId, userName, userEmail).fold(
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

            groupsRepository.joinGroup(inviteCode, currentUserId, userName, userEmail).fold(
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
    
    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            groupsRepository.deleteGroup(groupId, currentUserId).fold(
                onSuccess = { success ->
                    if (success) {
                        val updatedGroups = _uiState.value.groups.filter { it.id != groupId }
                        _uiState.update { 
                            it.copy(
                                groups = updatedGroups,
                                isLoading = false
                            )
                        }
                        
                        // Asegurarnos que la lista esté completamente actualizada
                        loadGroups()
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "No se pudo eliminar el grupo"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al eliminar el grupo"
                        )
                    }
                }
            )
        }
    }
    
    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            groupsRepository.leaveGroup(groupId, currentUserId).fold(
                onSuccess = { success ->
                    if (success) {
                        val updatedGroups = _uiState.value.groups.filter { it.id != groupId }
                        _uiState.update { 
                            it.copy(
                                groups = updatedGroups,
                                isLoading = false
                            )
                        }
                        
                        // Asegurarnos que la lista esté completamente actualizada
                        loadGroups()
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "No se pudo salir del grupo"
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al salir del grupo"
                        )
                    }
                }
            )
        }
    }
    
    fun isGroupOwner(group: GroupResponse): Boolean {
        return group.createdBy == currentUserId
    }
}

data class GroupsUiState(
    val groups: List<GroupResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
