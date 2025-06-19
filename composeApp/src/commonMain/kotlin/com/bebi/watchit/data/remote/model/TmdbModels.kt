package com.bebi.watchit.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbSearchResponse(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<TmdbMediaItem> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0
)

@Serializable
data class TmdbMediaItem(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null
) {
    // No marcamos como @Transient porque no afecta la serialización
    var watchProviders: List<String> = emptyList()
    
    val displayTitle: String
        get() = title ?: name ?: ""
        
    val displayReleaseDate: String
        get() = releaseDate ?: firstAirDate ?: ""
        
    val isMovie: Boolean
        get() = mediaType == "movie"
        
    val isTvShow: Boolean
        get() = mediaType == "tv"
}

@Serializable
data class TmdbGenre(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
data class TmdbGenresResponse(
    @SerialName("genres") val genres: List<TmdbGenre> = emptyList()
)

/**
 * Modelo para la respuesta de créditos de una película
 */
@Serializable
data class TmdbCreditsResponse(
    @SerialName("id") val id: Int,
    @SerialName("cast") val cast: List<TmdbCastMember> = emptyList(),
    @SerialName("crew") val crew: List<TmdbCrewMember> = emptyList()
)

/**
 * Modelo para un miembro del elenco
 */
@Serializable
data class TmdbCastMember(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("character") val character: String? = null,
    @SerialName("order") val order: Int = 0
)

/**
 * Modelo para un miembro del equipo de producción
 */
@Serializable
data class TmdbCrewMember(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("department") val department: String? = null,
    @SerialName("job") val job: String? = null
)
