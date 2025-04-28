package com.bebi.app.viewmodel

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

/**
 * ViewModel base para las pantallas de listado de medios de TMDB
 */
abstract class TmdbMediaListViewModel(
    protected val repository: TmdbRepository
) : ViewModel() {

    // Estado UI para la lista
    private val _uiState = MutableStateFlow(TmdbMediaListUiState())
    val uiState: StateFlow<TmdbMediaListUiState> = _uiState.asStateFlow()

    init {
        loadMediaList()
    }

    /**
     * Método abstracto que debe ser implementado por las subclases para cargar
     * los datos específicos de cada tipo de lista
     */
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
                                mediaItems = mediaOpinions
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
        return MediaOpinion(
            id = item.id.toLong(),
            title = item.displayTitle,
            posterUrl = item.posterPath?.let { repository.getFullPosterUrl(it) },
            backdropUrl = item.backdropPath?.let { repository.getFullBackdropUrl(it) },
            genre = item.genreIds.firstOrNull()?.toString() ?: "",
            platform = if (item.mediaType == "movie") "Película" else "Serie",
            rating = item.voteAverage?.toFloat() ?: 0f,
            averageRating = item.voteAverage?.toFloat() ?: 0f,
            synopsis = item.overview ?: ""
        )
    }
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

/**
 * Estado UI para las pantallas de listado de medios de TMDB
 */
data class TmdbMediaListUiState(
    val isLoading: Boolean = true,
    val mediaItems: List<MediaOpinion> = emptyList(),
    val error: String? = null
)
