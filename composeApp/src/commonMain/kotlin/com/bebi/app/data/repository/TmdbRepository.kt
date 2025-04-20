package com.bebi.app.data.repository

import com.bebi.app.data.remote.AppService
import com.bebi.app.data.remote.model.TmdbGenre
import com.bebi.app.data.remote.model.TmdbMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Repository to handle TMDB API operations
 */
class TmdbRepository(private val appService: AppService) {
    
    // Cache de géneros para evitar llamadas repetidas
    private var movieGenres: List<TmdbGenre>? = null
    private var tvGenres: List<TmdbGenre>? = null
    
    /**
     * Busca una película o serie por su nombre
     * @return El primer resultado encontrado o null si no se encontró nada
     */
    suspend fun searchMediaByTitle(title: String): Result<TmdbMediaItem?> = withContext(Dispatchers.IO) {
        try {
            val response = appService.searchMedia(title)
            if (response.results.isEmpty()) {
                Result.success(null)
            } else {
                Result.success(response.results.first())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los géneros de películas
     */
    private suspend fun getMovieGenres(): Result<List<TmdbGenre>> = withContext(Dispatchers.IO) {
        try {
            if (movieGenres != null) {
                Result.success(movieGenres!!)
            } else {
                val response = appService.getMovieGenres()
                movieGenres = response.genres
                Result.success(response.genres)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los géneros de series de TV
     */
    private suspend fun getTvGenres(): Result<List<TmdbGenre>> = withContext(Dispatchers.IO) {
        try {
            if (tvGenres != null) {
                Result.success(tvGenres!!)
            } else {
                val response = appService.getTvGenres()
                tvGenres = response.genres
                Result.success(response.genres)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los nombres de los géneros para una lista de IDs de género
     */
    suspend fun getGenreNames(genreIds: List<Int>, isMovie: Boolean): Result<List<String>> {
        val genresResult = if (isMovie) getMovieGenres() else getTvGenres()
        
        return genresResult.map { genres ->
            genreIds.mapNotNull { id ->
                genres.find { it.id == id }?.name
            }
        }
    }
    
    /**
     * Obtiene la URL completa de una imagen de poster
     */
    fun getFullPosterUrl(posterPath: String?): String? {
        return appService.getImageUrl(posterPath)
    }
}
