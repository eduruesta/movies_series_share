package com.bebi.watchit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Entidad para almacenar los datos de TMDB en caché local
 */
@Entity(tableName = "tmdb_cache")
@Serializable
data class TmdbCachedMedia(
    @PrimaryKey
    val id: Long,
    val mediaType: String, // "movie" o "tv"
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val fullPosterUrl: String?, // URL completa del poster
    val fullBackdropUrl: String?, // URL completa del backdrop
    val releaseDate: String,
    val voteAverage: Double,
    val genreIds: List<Int>,
    val category: String, // "trending_movies", "trending_series", "top_movies", "top_series", "upcoming_movies"
    val lastUpdated: Long = Clock.System.now().toEpochMilliseconds()
)
