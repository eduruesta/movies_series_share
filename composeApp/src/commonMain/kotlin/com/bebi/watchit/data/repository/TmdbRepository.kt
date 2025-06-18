package com.bebi.watchit.data.repository

import com.bebi.watchit.data.remote.AppService
import com.bebi.watchit.data.remote.model.Provider
import com.bebi.watchit.data.remote.model.TmdbCastMember
import com.bebi.watchit.data.remote.model.TmdbCreditsResponse
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
    suspend fun getTopRatedTvShows(page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTopRatedTvShows(page)
            val results = response.results.map { it.copy(mediaType = "tv") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las series en tendencia
     */
    suspend fun getTrendingTvShows(page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTrendingTvShows(page)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las películas en tendencia
     */
    suspend fun getTrendingMovies(page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTrendingMovies(page)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las películas mejor valoradas
     */
    suspend fun getTopRatedMovies(page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTopRatedMovies(page)
            val results = response.results.map { it.copy(mediaType = "movie") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene las próximas películas
     */
    suspend fun getUpcomingMovies(page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getUpcomingMovies(page)
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
     * @param movieId ID de la película en TMDB
     * @return Mapa de proveedores por país
     */
    suspend fun getMovieWatchProviders(movieId: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val userCountry = appService.getCountryCode()
            val response = appService.getMovieWatchProviders(movieId)
            
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

    /**
     * Obtiene los créditos de una película (elenco y equipo)
     * @param movieId ID de la película en TMDB
     * @return Respuesta con la información del elenco y equipo de la película
     */
    suspend fun getMovieCredits(movieId: Int): Result<TmdbCreditsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getMovieCredits(movieId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el elenco principal de una película (solo actores)
     * @param movieId ID de la película en TMDB
     * @param limit Número máximo de actores a devolver (opcional)
     * @return Lista con los actores de la película ordenados por importancia
     */
    suspend fun getMovieCast(movieId: Int, limit: Int? = null): Result<List<TmdbCastMember>> = withContext(Dispatchers.IO) {
        try {
            val credits = appService.getMovieCredits(movieId)
            val castList = if (limit != null) {
                credits.cast.take(limit)
            } else {
                credits.cast
            }
            Result.success(castList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los créditos de una serie (elenco y equipo)
     * @param tvId ID de la serie en TMDB
     * @return Respuesta con la información del elenco y equipo de la película
     */
    suspend fun getTvCredits(tvId: Int): Result<TmdbCreditsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTvCredits(tvId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el elenco principal de una serie (solo actores)
     * @param serieId ID de la película en TMDB
     * @param limit Número máximo de actores a devolver (opcional)
     * @return Lista con los actores de la serie ordenados por importancia
     */
    suspend fun getTvCast(tvId: Int, limit: Int? = null): Result<List<TmdbCastMember>> = withContext(Dispatchers.IO) {
        try {
            val credits = appService.getTvCredits(tvId)
            val castList = if (limit != null) {
                credits.cast.take(limit)
            } else {
                credits.cast
            }
            Result.success(castList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get cast for a TV show
     */
    suspend fun getTvShowCast(tvId: Int): Result<List<TmdbCastMember>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getTvCredits(tvId)
            Result.success(response.cast)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene películas recomendadas a una película específica
     * @param movieId ID de la película en TMDB
     * @param page Número de página para la paginación
     * @return Resultado con lista de películas recomendadas marcadas con tipo "movie"
     */
    suspend fun getRecommendationsMovies(movieId: Int, page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getRecommendationsMovies(movieId, page)
            val results = response.results.map { it.copy(mediaType = "movie") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene series recomendadas a una serie específica
     * @param tvId ID de la serie en TMDB
     * @param page Número de página para la paginación
     * @return Resultado con lista de series recomendadas marcadas con tipo "tv"
     */
    suspend fun getRecommendationsTvShows(tvId: Int, page: Int = 1): Result<List<TmdbMediaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = appService.getRecommendationsTvShows(tvId, page)
            val results = response.results.map { it.copy(mediaType = "tv") }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
