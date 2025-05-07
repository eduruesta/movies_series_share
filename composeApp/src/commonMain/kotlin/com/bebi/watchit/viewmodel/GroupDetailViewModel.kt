package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupDetailViewModel(
    private val groupId: String,
    private val mediaOpinionRepository: MediaOpinionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    init {
        loadOpinions()
    }

    private fun loadOpinions() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                mediaOpinionRepository.getAllOpinions()
                    .catch { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                    .collectLatest { allOpinions ->
                        val groupOpinions = allOpinions.filter { it.groupId == groupId }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                opinions = groupOpinions,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refreshOpinions() {
        loadOpinions()
    }
}

data class GroupDetailUiState(
    val isLoading: Boolean = false,
    val opinions: List<MediaOpinion> = emptyList(),
    val error: String? = null
)
