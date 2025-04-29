package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.MediaOpinionRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.CancellationException
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
    
    /**
     * Añade un nuevo comentario a la opinión actual.
     * Verifica si la opinión existe en el servidor antes de decidir si crear nueva o actualizar.
     */
    fun addComment(comment: String) {
        if (comment.isBlank()) return
        
        val currentOpinion = _uiState.value.opinion ?: return
        
        viewModelScope.launch {
            try {
                val existingOpinion = repository.getOpinionByIdDirect(currentOpinion.id)
                
                if (existingOpinion == null) {
                    val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)
                    
                    val newOpinion = MediaOpinion(
                        id = randomId,
                        title = currentOpinion.title,
                        platform = currentOpinion.platform,
                        genre = currentOpinion.genre,
                        rating = currentOpinion.rating,
                        comments = listOf(comment),
                        synopsis = currentOpinion.synopsis,
                        posterUrl = currentOpinion.posterUrl,
                        ratingCount = 1,
                        averageRating = currentOpinion.rating,
                        year = currentOpinion.year,
                        backdropUrl = currentOpinion.backdropUrl
                    )
                    
                    val savedId = repository.saveOpinion(newOpinion)
                    if (savedId > 0) {
                        loadOpinionById(savedId)
                        _uiState.update { it.copy(newComment = "") }
                    } else {
                        _uiState.update { it.copy(commentError = "No se pudo guardar la opinión") }
                    }
                } else {
                    val updatedComments = existingOpinion.comments.toMutableList().apply {
                        add(comment)
                    }
                    
                    val updatedOpinion = existingOpinion.copy(comments = updatedComments)
                    
                    val success = repository.updateOpinionById(existingOpinion.id, updatedOpinion)
                    if (success) {
                        _uiState.update { it.copy(opinion = updatedOpinion, newComment = "") }
                    } else {
                        _uiState.update { it.copy(commentError = "No se pudo actualizar la opinión") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(commentError = "Error al añadir el comentario: ${e.message}") }
            }
        }
    }
    
    /**
     * Actualiza el texto del nuevo comentario
     */
    fun updateNewComment(comment: String) {
        _uiState.update { it.copy(newComment = comment) }
    }
}

/**
 * UI state for the Media Detail screen
 */
data class MediaDetailUiState(
    val opinion: MediaOpinion? = null,
    val isLoading: Boolean = false,
    val error: MediaDetailError? = null,
    val newComment: String = "",
    val commentError: String? = null
)

/**
 * Tipos de errores para la pantalla de detalle
 * Estos se traducirán a strings en la UI
 */
enum class MediaDetailError {
    NOT_FOUND,
    GENERIC
}
