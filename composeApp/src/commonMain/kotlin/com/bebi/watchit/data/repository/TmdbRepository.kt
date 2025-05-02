package com.bebi.watchit.data.repository

import com.bebi.watchit.data.remote.AppService
import com.bebi.watchit.data.remote.model.Provider
import com.bebi.watchit.data.remote.model.TmdbGenre
import com.bebi.watchit.data.remote.model.TmdbMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Repository to handle TMDB API operations
 */
class TmdbRepository(private val appService: AppService) {
    
    private var movieGenres: List<TmdbGenre>? = null
    private var tvGenres: List<TmdbGenre>? = null
    
    /**
     * Busca una película o serie por su nombre
     * @return La lista de resultados encontrados o lista vacía si no se encontró nada
     */
    suspend fun searchMediaByTitle(title: String): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.searchMedia(title)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las series mejor valoradas
     */
    suspend fun getTopRatedTvShows(): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTopRatedTvShows()
            val results = response.results.map { it.copy(mediaType = "tv") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las series en tendencia
     */
    suspend fun getTrendingTvShows(): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTrendingTvShows()
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las películas en tendencia
     */
    suspend fun getTrendingMovies(): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTrendingMovies()
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las películas mejor valoradas
     */
    suspend fun getTopRatedMovies(): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTopRatedMovies()
            val results = response.results.map { it.copy(mediaType = "movie") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las próximas películas
     */
    suspend fun getUpcomingMovies(): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getUpcomingMovies()
            val results = response.results.map { it.copy(mediaType = "movie") }
            Result.success(results)
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

    fun getFullBackdropUrl(posterPath: String?): String? {
        return appService.getBackdropUrl(posterPath)
    }
    
    /**
     * Obtiene los proveedores de streaming para una película
     * @return Lista de nombres de plataformas disponibles en el país del usuario
     */
    suspend fun getMovieWatchProviders(movieId: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val userCountry = appService.getCountryCode()
            val response = appService.getMovieWatchProviders(movieId)
            
            val countryProviders = response.results[userCountry]
            
            // Recopilar todas las plataformas de streaming, alquiler y compra
            val providers = mutableListOf<Provider>()
            countryProviders?.flatrate?.let { providers.addAll(it) }
            countryProviders?.rent?.let { providers.addAll(it) }
            countryProviders?.buy?.let { providers.addAll(it) }
            
            // Devolver los nombres de los proveedores
            val providerNames = providers.map { it.providerName }.distinct()
            Result.success(providerNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los proveedores de streaming para una serie
     * @return Lista de nombres de plataformas disponibles en el país del usuario
     */
    suspend fun getTvWatchProviders(tvId: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val userCountry = appService.getCountryCode()
            val response = appService.getTvWatchProviders(tvId)
            
            val countryProviders = response.results[userCountry]
            
            val providers = mutableListOf<Provider>()
            countryProviders?.flatrate?.let { providers.addAll(it) }
            countryProviders?.rent?.let { providers.addAll(it) }
            countryProviders?.buy?.let { providers.addAll(it) }
            
            val providerNames = providers.map { it.providerName }.distinct()
            Result.success(providerNames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
