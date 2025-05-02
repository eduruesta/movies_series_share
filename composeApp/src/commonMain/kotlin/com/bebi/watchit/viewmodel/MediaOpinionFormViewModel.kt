package com.bebi.watchit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.remote.model.TmdbMediaItem
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.data.repository.TmdbRepository
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el formulario de creación/edición de opiniones
 */
class MediaOpinionFormViewModel(
    private val repository: MediaOpinionRepository,
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

    // Cambiado de comment a comments como String (se convertirá a lista al guardar)
    var comments by mutableStateOf("")
        private set

    var synopsis by mutableStateOf("")
        private set

    var posterUrl by mutableStateOf("")
        private set

    var backdropUrl by mutableStateOf("")
        private set

    var year by mutableStateOf("")
        private set

    // Estado de búsqueda
    var isSearching by mutableStateOf(false)
        private set

    // Tipos de mensajes que pueden mostrarse en la UI
    sealed class SearchUiMessage {
        data class NoResults(val query: String): SearchUiMessage()
        data class ResultsCount(val count: Int, val query: String): SearchUiMessage()
        data class Error(val error: String): SearchUiMessage()
        data class Selected(val title: String): SearchUiMessage()
        data class Generic(val text: String): SearchUiMessage()
        object None: SearchUiMessage()
    }
    
    // Estado de mensaje de búsqueda
    private val _searchUiMessage = MutableStateFlow<SearchUiMessage>(SearchUiMessage.None)
    val searchUiMessage: StateFlow<SearchUiMessage> = _searchUiMessage

    // Lista de resultados de búsqueda
    var searchResults by mutableStateOf<List<TmdbMediaItem>>(emptyList())
        private set

    // Variable para controlar si se debe mostrar el dropdown
    var showSearchResults by mutableStateOf(false)
        private set

    /**
     * Realiza una búsqueda de películas/series por título
     */
    fun searchMedia(query: String) {
        if (query.isBlank()) return

        isSearching = true
        _searchUiMessage.value = SearchUiMessage.None
        searchResults = emptyList()
        showSearchResults = false

        viewModelScope.launch {
            tmdbRepository.searchMediaByTitle(query).fold(
                onSuccess = { results ->
                    if (results.isEmpty()) {
                        _searchUiMessage.value = SearchUiMessage.NoResults(query)
                    } else {
                        searchResults = results
                        showSearchResults = true
                        _searchUiMessage.value = SearchUiMessage.ResultsCount(results.size, query)
                    }
                    isSearching = false
                },
                onFailure = { error ->
                    searchResults = emptyList()
                    _searchUiMessage.value = SearchUiMessage.Error(error.message ?: "")
                    isSearching = false
                }
            )
        }
    }

    /**
     * Selecciona un item de los resultados de búsqueda
     */
    fun selectMediaItem(mediaItem: TmdbMediaItem) {
        viewModelScope.launch {
            fillFormWithMediaItem(mediaItem)
            showSearchResults = false
            _searchUiMessage.value = SearchUiMessage.Selected(mediaItem.displayTitle)
        }
    }

    /**
     * Cierra el dropdown de resultados
     */
    fun closeSearchResults() {
        showSearchResults = false
    }

    /**
     * Actualiza el formulario con datos del API
     */
    private suspend fun fillFormWithMediaItem(mediaItem: TmdbMediaItem) {
        title = mediaItem.displayTitle
        synopsis = mediaItem.overview ?: ""
        year = mediaItem.displayReleaseDate

        val fullUrl = tmdbRepository.getFullPosterUrl(mediaItem.posterPath)
        val fullBackdropUrl = tmdbRepository.getFullBackdropUrl(mediaItem.backdropPath)

        fullUrl?.let {
            posterUrl = it
        }

        fullBackdropUrl?.let {
            backdropUrl = it
        }

        val watchProvidersResult = if (mediaItem.isMovie) {
            tmdbRepository.getMovieWatchProviders(mediaItem.id)
        } else {
            tmdbRepository.getTvWatchProviders(mediaItem.id)
        }
        
        watchProvidersResult.fold(
            onSuccess = { providers ->
                if (providers.isNotEmpty()) {
                    platform = providers.firstOrNull() ?: ""
                }
            },
            onFailure = { /* Mantener valor actual */ }
        )

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
        comments = newComment
    }

    fun updateSynopsis(newSynopsis: String) {
        synopsis = newSynopsis
    }

    fun updatePosterUrl(newUrl: String?) {
        if (newUrl != null) {
            posterUrl = newUrl
        }
    }

    /**
     * Guarda la opinión
     */
    fun saveOpinion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)

            val commentsList = if (comments.isBlank()) {
                emptyList()
            } else {
                listOf(comments)
            }

            val opinion = MediaOpinion(
                id = randomId,
                title = title,
                platform = platform,
                genre = genre,
                rating = rating,
                comments = commentsList,
                synopsis = synopsis,
                posterUrl = posterUrl,
                ratingCount = 1,
                averageRating = rating,
                year = year,
                backdropUrl = backdropUrl
            )

            repository.saveOpinion(opinion)
            _uiState.value = _uiState.value.copy(
                saved = true,
                isSaving = false
            )
        }
    }

    /**
     * Resetea el formulario
     */
    private fun resetForm() {
        title = ""
        platform = ""
        genre = ""
        rating = 0f
        comments = ""
        synopsis = ""
        posterUrl = ""
        _uiState.value = MediaOpinionFormUiState()
    }

    fun resetSavedState() {
        resetForm()
    }

}

data class MediaOpinionFormUiState(
    val saved: Boolean = false,
    val isSaving: Boolean = false
)
