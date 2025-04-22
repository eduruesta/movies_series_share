package com.bebi.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.remote.model.TmdbMediaItem
import com.bebi.app.data.repository.RoomMediaOpinionRepository
import com.bebi.app.data.repository.TmdbRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MediaOpinionFormViewModel(
    private val mediaOpinionRepository: RoomMediaOpinionRepository,
    private val tmdbRepository: TmdbRepository
) : ViewModel() {
    // Estado UI para el formulario
    private val _uiState = MutableStateFlow(MediaOpinionFormUiState())
    val uiState: StateFlow<MediaOpinionFormUiState> = _uiState.asStateFlow()

    // Campos individuales del formulario
    var title by mutableStateOf("")
        private set

    var platform by mutableStateOf("")
        private set

    var genre by mutableStateOf("")
        private set

    var rating by mutableStateOf(0f)
        private set

    var comment by mutableStateOf("")
        private set

    var synopsis by mutableStateOf("")
        private set

    var posterUrl by mutableStateOf("")
        private set

    // Estado de búsqueda
    var isSearching by mutableStateOf(false)
        private set

    var searchError by mutableStateOf<String?>(null)
        private set

    // Función para buscar película o serie según título
    fun searchMedia(query: String) {
        if (query.isBlank()) return

        isSearching = true
        searchError = null

        viewModelScope.launch {
            tmdbRepository.searchMediaByTitle(query).fold(
                onSuccess = { mediaItem ->
                    if (mediaItem == null) {
                        _uiState.value = _uiState.value.copy(
                            searchMessage = "No se encontraron resultados para \"$query\""
                        )
                    } else {
                        fillFormWithMediaItem(mediaItem)
                        _uiState.value = _uiState.value.copy(
                            searchMessage = "Se encontró \"${mediaItem.displayTitle}\"",
                        )
                    }
                    isSearching = false
                },
                onFailure = { error ->
                    searchError = "Error al buscar: ${error.message}"
                    isSearching = false
                }
            )
        }
    }

    // Actualiza el formulario con datos del API
    private suspend fun fillFormWithMediaItem(mediaItem: TmdbMediaItem) {
        title = mediaItem.displayTitle
        synopsis = mediaItem.overview ?: ""

        // Obtener la URL del póster y manejar correctamente valores null
        println("DEBUG: Poster path: ${mediaItem.posterPath}")
        val fullUrl = tmdbRepository.getFullPosterUrl(mediaItem.posterPath)
        println("DEBUG: Full poster URL: $fullUrl")

        fullUrl?.let {
            posterUrl = it
            println("DEBUG: posterUrl actualizado a: $posterUrl")
        }

        // Obtener géneros
        val genres = if (mediaItem.isMovie) {
            tmdbRepository.getGenreNames(mediaItem.genreIds, isMovie = true)
        } else {
            tmdbRepository.getGenreNames(mediaItem.genreIds, isMovie = false)
        }

        genres.fold(
            onSuccess = { genreNames ->
                genre = genreNames.joinToString(", ")
            },
            onFailure = { /* Mantener valor actual */ }
        )
    }

    // Funciones para actualizar cada campo
    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    fun updatePlatform(newPlatform: String) {
        platform = newPlatform
    }

    fun updateGenre(newGenre: String) {
        genre = newGenre
    }

    fun updateRating(newRating: Float) {
        rating = newRating
    }

    fun updateComment(newComment: String) {
        comment = newComment
    }

    fun updateSynopsis(newSynopsis: String) {
        synopsis = newSynopsis
    }

    fun updatePosterUrl(newUrl: String?) {
        if (newUrl != null) {
            posterUrl = newUrl
        }
    }

    // Guarda la opinión
    fun saveOpinion() {
        viewModelScope.launch {
            val opinion = MediaOpinion(
                title = title,
                platform = platform,
                genre = genre,
                rating = rating,
                comment = comment,
                synopsis = synopsis,
                posterUrl = posterUrl,
                ratingCount = 1,
                averageRating = rating
            )

            mediaOpinionRepository.saveOpinion(opinion)
            _uiState.value = _uiState.value.copy(
                saved = true
            )
        }
    }

    // Resetea el formulario
    private fun resetForm() {
        title = ""
        platform = ""
        genre = ""
        rating = 0f
        comment = ""
        synopsis = ""
        posterUrl = ""
        // Crear una nueva instancia del estado UI para asegurarnos de que saved sea false
        _uiState.value = MediaOpinionFormUiState()
    }

    fun resetSavedState() {
        resetForm()
    }

}

data class MediaOpinionFormUiState(
    val saved: Boolean = false,
    val searchMessage: String? = null
)
