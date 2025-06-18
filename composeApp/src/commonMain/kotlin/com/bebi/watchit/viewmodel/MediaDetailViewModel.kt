package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.data.repository.TmdbRepository
import com.bebi.watchit.data.remote.model.TmdbCastMember
import com.bebi.watchit.data.remote.model.TmdbMediaItem
import com.bebi.watchit.model.Comment
import com.bebi.watchit.model.MediaOpinion
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
    private val repository: MediaOpinionRepository,
    private val tmdbRepository: TmdbRepository
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
     * Carga el elenco de una película o serie.
     * Se puede llamar con un ID de TMDB sin necesidad de tener la opinión cargada.
     * @param tmdbId El ID de TMDB de la película o serie
     * @param mediaType Tipo de medio (movie o tv)
     */
    fun loadCast(tmdbId: Int, mediaType: String? = null) {
        val isMovieMedia = mediaType == "movie"
        // Indicar que estamos cargando el elenco
        _uiState.update { it.copy(isLoadingCast = true) }

        viewModelScope.launch {
            try {
                val castResult = if (isMovieMedia) {
                    tmdbRepository.getMovieCast(tmdbId)
                } else {
                    tmdbRepository.getTvShowCast(tmdbId)
                }

                castResult.fold(
                    onSuccess = { cast ->
                        _uiState.update {
                            it.copy(
                                cast = cast,
                                isLoadingCast = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                cast = emptyList(),
                                isLoadingCast = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cast = emptyList(),
                        isLoadingCast = false
                    )
                }
            }
        }
    }

    /**
     * Carga medios recomendadas para una película o serie
     * @param tmdbId El ID de TMDB del medio
     * @param mediaType Tipo de medio ("movie" o "tv")
     */
    fun loadRecommendationsMedia(tmdbId: Int, mediaType: String? = null) {
        val isMovieMedia = mediaType == "movie"
        
        // Indicar que estamos cargando medios similares
        _uiState.update { it.copy(isLoadingRecommendationsMedia = true) }
        
        viewModelScope.launch {
            try {
                val similarResult = if (isMovieMedia) {
                    tmdbRepository.getRecommendationsMovies(tmdbId)
                } else {
                    tmdbRepository.getRecommendationsTvShows(tmdbId)
                }
                
                similarResult.fold(
                    onSuccess = { similarMedia ->
                        _uiState.update {
                            it.copy(
                                recommendationsMedia = similarMedia,
                                isLoadingRecommendationsMedia = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                recommendationsMedia = emptyList(),
                                isLoadingRecommendationsMedia = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        recommendationsMedia = emptyList(),
                        isLoadingRecommendationsMedia = false
                    )
                }
            }
        }
    }

    /**
     * Establece directamente un MediaOpinion para elementos TMDB
     * que no están guardados en la base de datos.
     * Este método se llama cuando el usuario selecciona una película/serie de TMDB en la home.
     */
    fun setTmdbMediaOpinion(opinion: MediaOpinion? = null) {
        _uiState.update { it.copy(opinion = opinion, isLoading = false) }
    }

    /**
     * Añade un nuevo comentario a la opinión actual.
     * Verifica si la opinión existe en el servidor antes de decidir si crear nueva o actualizar.
     */
    fun addComment(commentText: String, username: String) {
        if (commentText.isBlank()) return

        val currentOpinion = _uiState.value.opinion ?: return

        // Crear el objeto Comment con la información del backend
        val comment = Comment(
            text = commentText,
            username = username
        )

        viewModelScope.launch {
            try {
                val existingOpinion = repository.getOpinionByIdDirect(currentOpinion.id)

                if (existingOpinion == null) {

                    val newOpinion = MediaOpinion(
                        id = currentOpinion.id,
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
                        _uiState.update { it.copy(newComment = "", username = "") }
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
                        _uiState.update {
                            it.copy(
                                opinion = updatedOpinion,
                                newComment = "",
                                username = ""
                            )
                        }
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
        _uiState.update { it.copy(newComment = comment, commentError = null) }
    }
    
    /**
     * Actualiza el nombre del autor del nuevo comentario
     */
    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username, commentError = null) }
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
    val username: String = "",
    val commentError: String? = null,
    val cast: List<TmdbCastMember>? = null,
    val isLoadingCast: Boolean = false,
    val recommendationsMedia: List<TmdbMediaItem> = emptyList(),
    val isLoadingRecommendationsMedia: Boolean = false
)

/**
 * Tipos de errores para la pantalla de detalle
 * Estos se traducirán a strings en la UI
 */
enum class MediaDetailError {
    NOT_FOUND,
    GENERIC
}
