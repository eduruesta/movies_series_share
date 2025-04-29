package com.bebi.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.remote.model.TmdbMediaItem
import com.bebi.app.data.repository.TmdbRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * ViewModel base para las pantallas de listado de medios de TMDB
 */
abstract class TmdbMediaListViewModel(
    protected val repository: TmdbRepository
) : ViewModel() {

    // Estado UI para la lista
    private val _uiState = MutableStateFlow(TmdbMediaListUiState())
    val uiState: StateFlow<TmdbMediaListUiState> = _uiState.asStateFlow()
    var genre by mutableStateOf("")
        private set

    init {
        loadMediaList()
    }


    protected abstract suspend fun loadMediaItems(): Result<List<TmdbMediaItem>>

    /**
     * Carga la lista de medios según la implementación específica
     */
    private fun loadMediaList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val result = loadMediaItems()
                result.fold(
                    onSuccess = { items ->
                        val mediaOpinions = items.map { convertToMediaOpinion(it) }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                mediaItems = mediaOpinions,
                                filteredMediaItems = mediaOpinions
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Error al cargar los datos"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado"
                    )
                }
            }
        }
    }

    /**
     * Convierte un TmdbMediaItem a MediaOpinion para usar en la UI
     */
    private fun convertToMediaOpinion(item: TmdbMediaItem): MediaOpinion {
        // Valor inicial del género (se actualizará más tarde)
        val initialGenre = item.genreIds.firstOrNull()?.toString() ?: ""
        
        // Determinamos si es película o serie
        val isMovie = item.mediaType == "movie"
        
        // Creamos el objeto MediaOpinion con la información básica
        val mediaOpinion = MediaOpinion(
            id = item.id.toLong(),
            title = item.displayTitle,
            posterUrl = item.posterPath?.let { repository.getFullPosterUrl(it) },
            backdropUrl = item.backdropPath?.let { repository.getFullBackdropUrl(it) },
            genre = initialGenre,
            platform = if (isMovie) "Movie" else "Serie",
            rating = item.voteAverage?.toFloat() ?: 0f,
            averageRating = item.voteAverage?.toFloat() ?: 0f,
            synopsis = item.overview ?: "",
            year = item.displayReleaseDate
        )
        
        // Iniciamos una consulta asíncrona para obtener los nombres de los géneros
        viewModelScope.launch {
            val genresResult = repository.getGenreNames(item.genreIds, isMovie = isMovie)
            
            genresResult.fold(
                onSuccess = { genreNames ->
                    if (genreNames.isNotEmpty()) {
                        // Actualizamos el estado con los nombres de géneros
                        _uiState.update { currentState ->
                            val updatedMediaItems = currentState.mediaItems.map { opinion ->
                                // Solo actualizamos el elemento actual
                                if (opinion.id == item.id.toLong()) {
                                    opinion.copy(genre = genreNames.joinToString(", "))
                                } else {
                                    opinion
                                }
                            }
                            currentState.copy(mediaItems = updatedMediaItems)
                        }
                    }
                },
                onFailure = { /* Mantener valor actual */ }
            )
        }
        
        return mediaOpinion
    }

    /**
     * Actualiza la consulta de búsqueda y filtra los elementos de medios
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isNotEmpty()) {
                filterMediaItems(currentState.mediaItems, query)
            } else {
                currentState.mediaItems
            }
            currentState.copy(
                searchQuery = query,
                filteredMediaItems = filtered
            )
        }
    }
    
    /**
     * Filtra los elementos de medios basándose en la consulta de búsqueda
     */
    private fun filterMediaItems(mediaItems: List<MediaOpinion>, query: String): List<MediaOpinion> {
        if (query.isBlank()) return mediaItems
        
        val lowercaseQuery = query.lowercase()
        return mediaItems.filter { mediaItem ->
            mediaItem.title.lowercase().contains(lowercaseQuery) ||
            mediaItem.genre.lowercase().contains(lowercaseQuery) ||
            mediaItem.year.lowercase().contains(lowercaseQuery)
        }
    }

    /**
     * Estado UI para las pantallas de listado de medios de TMDB
     */
    data class TmdbMediaListUiState(
        val isLoading: Boolean = true,
        val mediaItems: List<MediaOpinion> = emptyList(),
        val filteredMediaItems: List<MediaOpinion> = emptyList(),
        val searchQuery: String = "",
        val error: String? = null
    )
}

/**
 * ViewModel para la pantalla de series mejor valoradas
 */
class TopSeriesViewModel(repository: TmdbRepository) : TmdbMediaListViewModel(repository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedTvShows()
    }
}

/**
 * ViewModel para la pantalla de series en tendencia
 */
class TrendingSeriesViewModel(repository: TmdbRepository) : TmdbMediaListViewModel(repository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTrendingTvShows()
    }
}

/**
 * ViewModel para la pantalla de películas próximas a estrenarse
 */
class UpcomingMoviesViewModel(repository: TmdbRepository) : TmdbMediaListViewModel(repository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getUpcomingMovies()
    }
}

/**
 * ViewModel para la pantalla de películas mejor valoradas
 */
class TopMoviesViewModel(repository: TmdbRepository) : TmdbMediaListViewModel(repository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedMovies()
    }
}

/**
 * ViewModel para la pantalla de películas en tendencia
 */
class TrendingMoviesViewModel(repository: TmdbRepository) : TmdbMediaListViewModel(repository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTrendingMovies()
    }
}
