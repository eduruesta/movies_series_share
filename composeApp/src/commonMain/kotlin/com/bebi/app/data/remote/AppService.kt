package com.bebi.app.data.remote

import com.bebi.app.BuildConfig
import com.bebi.app.data.myLang
import com.bebi.app.data.remote.model.TmdbGenresResponse
import com.bebi.app.data.remote.model.TmdbSearchResponse
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
     * Get the full image URL from a poster path
     */
    fun getImageUrl(posterPath: String?): String? {
        return posterPath?.let { "$imageBaseUrl$it" }
    }

    fun getBackdropUrl(backdropPath: String?): String? {
        return backdropPath?.let { "$backdropUrl$it" }
    }

    private fun getLanguage(): String {
        return if (myLang == "es") {
            "es-ES"
        } else {
            "en-US"
        }
    }
}
