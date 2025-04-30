package com.bebi.watchit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.remote.model.TmdbMediaItem
import com.bebi.watchit.data.repository.TmdbRepository
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel base para las pantallas de listado de medios de TMDB
 */
abstract class TmdbMediaListViewModel(
    protected val repository: TmdbRepository,
    private val opinionRepository: MediaOpinionRepository
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
     * Actualiza el query de búsqueda y filtra los resultados
     */
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            val newQuery = query.trim()
            val currentItems = uiState.value.mediaItems
            
            val filteredItems = if (newQuery.isEmpty()) {
                currentItems
            } else {
                currentItems.filter { opinion ->
                    opinion.title.contains(newQuery, ignoreCase = true) || 
                    opinion.synopsis.contains(newQuery, ignoreCase = true)
                }
            }
            
            _uiState.update { 
                it.copy(
                    searchQuery = newQuery,
                    filteredMediaItems = filteredItems
                )
            }
        }
    }
    
    /**
     * Maneja el proceso de calificación de un medio TMDB
     * 
     * @param mediaOpinion Opinión del medio a calificar
     * @param rating Calificación del usuario (1-10)
     * @param onComplete Callback con el resultado (éxito o fracaso)
     */
    fun submitRating(mediaOpinion: MediaOpinion, rating: Int, onComplete: (Boolean) -> Unit) {
        _uiState.update { it.copy(isRating = true) }
        
        viewModelScope.launch {
            try {
                // Verificamos si la opinión existe en el remoto
                val existingOpinion = opinionRepository.getOpinionByIdDirect(mediaOpinion.id)

                if (existingOpinion == null) {
                    // La opinión no existe - Crear y guardar nueva opinión
                    val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)
                    
                    val newOpinion = MediaOpinion(
                        id = randomId,
                        title = mediaOpinion.title,
                        platform = mediaOpinion.platform,
                        genre = mediaOpinion.genre,
                        rating = rating.toFloat(),  // La calificación que acaba de dar el usuario
                        comments = emptyList(),      // Sin comentarios iniciales
                        synopsis = mediaOpinion.synopsis,
                        posterUrl = mediaOpinion.posterUrl,
                        ratingCount = 1,             // Primera calificación
                        averageRating = rating.toFloat(), // La primera calificación es el promedio
                        year = mediaOpinion.year,
                        backdropUrl = mediaOpinion.backdropUrl
                    )
                    
                    val savedId = opinionRepository.saveOpinion(newOpinion)
                    if (savedId > 0) {
                        _uiState.update { it.copy(isRating = false) }
                        onComplete(true)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "No se pudo guardar la calificación",
                                isRating = false
                            )
                        }
                        onComplete(false)
                    }
                } else {
                    // La opinión existe - Actualizar la calificación
                    val newRatingCount = existingOpinion.ratingCount + 1
                    val totalRatingPoints = existingOpinion.averageRating * existingOpinion.ratingCount + rating
                    val newAverageRating = totalRatingPoints / newRatingCount

                    val updatedOpinion = existingOpinion.copy(
                        ratingCount = newRatingCount,
                        averageRating = newAverageRating
                    )

                    val success = opinionRepository.updateOpinionById(existingOpinion.id, updatedOpinion)

                    if (success) {
                        _uiState.update { it.copy(isRating = false) }
                        onComplete(true)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "No se pudo actualizar la calificación",
                                isRating = false
                            )
                        }
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        isRating = false
                    )
                }
                onComplete(false)
            }
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
        val error: String? = null,
        val isRating: Boolean = false
    )
}

/**
 * ViewModel para la pantalla de series mejor valoradas
 */
class TopSeriesViewModel(repository: TmdbRepository, opinionRepository: MediaOpinionRepository) : TmdbMediaListViewModel(repository, opinionRepository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedTvShows()
    }
}

/**
 * ViewModel para la pantalla de series en tendencia
 */
class TrendingSeriesViewModel(repository: TmdbRepository, opinionRepository: MediaOpinionRepository) : TmdbMediaListViewModel(repository, opinionRepository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTrendingTvShows()
    }
}

/**
 * ViewModel para la pantalla de películas próximas a estrenarse
 */
class UpcomingMoviesViewModel(repository: TmdbRepository, opinionRepository: MediaOpinionRepository) : TmdbMediaListViewModel(repository, opinionRepository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getUpcomingMovies()
    }
}

/**
 * ViewModel para la pantalla de películas mejor valoradas
 */
class TopMoviesViewModel(repository: TmdbRepository, opinionRepository: MediaOpinionRepository) : TmdbMediaListViewModel(repository, opinionRepository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedMovies()
    }
}

/**
 * ViewModel para la pantalla de películas en tendencia
 */
class TrendingMoviesViewModel(repository: TmdbRepository, opinionRepository: MediaOpinionRepository) : TmdbMediaListViewModel(repository, opinionRepository) {
    override suspend fun loadMediaItems(): Result<List<TmdbMediaItem>> {
        return repository.getTrendingMovies()
    }
}
