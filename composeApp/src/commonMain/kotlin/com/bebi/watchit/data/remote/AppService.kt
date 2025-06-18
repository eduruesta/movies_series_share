package com.bebi.watchit.data.remote

import com.bebi.app.watchit.BuildConfig
import com.bebi.watchit.data.myCountry
import com.bebi.watchit.data.myLang
import com.bebi.watchit.data.remote.model.TmdbCreditsResponse
import com.bebi.watchit.data.remote.model.TmdbGenresResponse
import com.bebi.watchit.data.remote.model.TmdbSearchResponse
import com.bebi.watchit.data.remote.model.TmdbWatchProvidersResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments

/**
 * Service to interact with The Movie Database API
 */
class AppService(
    private val client: HttpClient
) {
    private val baseUrl = "api.themoviedb.org"
    private val apiVersion = "3"
    private val language = getLanguage()
    private val imageBaseUrl = "https://image.tmdb.org/t/p/w185"
    private val backdropUrl = "https://image.tmdb.org/t/p/w780"


    /**
     * Search for movies and TV shows by query
     */
    suspend fun searchMedia(query: String): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "search", "multi")
            parameters.append("query", query)
            parameters.append("include_adult", "false")
            parameters.append("language", language)
            parameters.append("page", "1")
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get movie genres
     */
    suspend fun getMovieGenres(): TmdbGenresResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "genre", "movie", "list")
            parameters.append("language", language)
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get TV show genres
     */
    suspend fun getTvGenres(): TmdbGenresResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "genre", "tv", "list")
            parameters.append("language", language)
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Construye la URL completa para una imagen de poster
     */
    fun getImageUrl(posterPath: String?): String? {
        if (posterPath == null) return null
        return "$imageBaseUrl$posterPath"
    }
    
    /**
     * Construye la URL completa para una imagen de fondo
     */
    fun getBackdropUrl(backdropPath: String?): String? {
        if (backdropPath == null) return null
        return "$backdropUrl$backdropPath"
    }

    private fun getLanguage(): String {
        return if (myLang == "es") {
            "es-AR"
        } else {
            "en-US"
        }
    }
    
    /**
     * Obtiene el código de país del usuario
     * @return código de país o "US" por defecto si no se puede determinar
     */
    fun getCountryCode(): String {
        return myCountry ?: "US"
    }
    
    /**
     * Get trending TV shows for the week
     */
    suspend fun getTrendingTvShows(page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "trending", "tv", "week")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get trending movies for the week
     */
    suspend fun getTrendingMovies(page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "trending", "movie", "week")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get top rated movies
     */
    suspend fun getTopRatedMovies(page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "movie", "top_rated")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get upcoming movies
     */
    suspend fun getUpcomingMovies(page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "movie", "upcoming")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Get top rated TV shows
     */
    suspend fun getTopRatedTvShows(page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "tv", "top_rated")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
    
    /**
     * Obtiene los proveedores de streaming para una película
     * @param movieId ID de la película en TMDB
     * @return Respuesta con los proveedores disponibles por país
     */
    suspend fun getMovieWatchProviders(movieId: Int): TmdbWatchProvidersResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "movie", movieId.toString(), "watch", "providers")
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
    
    /**
     * Obtiene los proveedores de streaming para una serie
     * @param tvId ID de la serie en TMDB
     * @return Respuesta con los proveedores disponibles por país
     */
    suspend fun getTvWatchProviders(tvId: Int): TmdbWatchProvidersResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "tv", tvId.toString(), "watch", "providers")
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
    
    /**
     * Obtiene los créditos (elenco y equipo) de una película
     * @param movieId ID de la película en TMDB
     * @return Respuesta con la información del elenco y equipo de la película
     */
    suspend fun getMovieCredits(movieId: Int): TmdbCreditsResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "movie", movieId.toString(), "credits")
            parameters.append("language", language)
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }

    /**
     * Obtiene los créditos (elenco y equipo) de una serie
     * @param serieId ID de la serie en TMDB
     * @return Respuesta con la información del elenco y equipo de la serie
     */
    suspend fun getTvCredits(tvId: Int): TmdbCreditsResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "tv", tvId.toString(), "credits")
            parameters.append("language", language)
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
    
    /**
     * Obtiene películas similares a una película específica
     * @param movieId ID de la película en TMDB
     * @param page Número de página para la paginación
     * @return Respuesta con la lista de películas recomendadas
     */
    suspend fun getRecommendationsMovies(movieId: Int, page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "movie", movieId.toString(), "recommendations")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
    
    /**
     * Obtiene series similares a una serie específica
     * @param tvId ID de la serie en TMDB
     * @param page Número de página para la paginación
     * @return Respuesta con la lista de series recomendadas
     */
    suspend fun getRecommendationsTvShows(tvId: Int, page: Int = 1): TmdbSearchResponse {
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            host = baseUrl
            appendPathSegments(apiVersion, "tv", tvId.toString(), "recommendations")
            parameters.append("language", language)
            parameters.append("page", page.toString())
            parameters.append("api_key", BuildConfig.api_key)
        }.build()

        return client.get(url).body()
    }
}
