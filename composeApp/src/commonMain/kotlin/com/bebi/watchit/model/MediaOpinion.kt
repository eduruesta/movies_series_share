package com.bebi.watchit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bebi.watchit.data.JavaSerializable
import com.bebi.watchit.model.Comment
import kotlinx.serialization.Serializable

/**
 * Model class for storing movie or series opinions
 */
@Serializable
@Entity(
    tableName = "media_opinions",
    indices = [Index(value = ["title"], unique = true)]
)
data class MediaOpinion(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    @ColumnInfo(name = "comments")
    val comments: List<Comment> = mutableListOf(),
    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",
    @ColumnInfo(name = "genre")
    val genre: String = "",
    @ColumnInfo(name = "platform")
    val platform: String = "",
    @ColumnInfo(name = "year")
    val year: String = "",
    @ColumnInfo(name = "duration")
    val duration: String = "",
    @ColumnInfo(name = "content_rating")
    val contentRating: String = "",
    @ColumnInfo(name = "synopsis")
    val synopsis: String = "",
    @ColumnInfo(name = "poster_url")
    val posterUrl: String? = null,

    @ColumnInfo(name = "media_type")
    val mediaType: String? = null,

    @ColumnInfo(name = "backdrop_url")
    val backdropUrl: String? = null,
    /**
     * Número total de usuarios que han calificado esta película/serie
     */
    @ColumnInfo(name = "rating_count")
    val ratingCount: Int = 1,
    /**
     * Calificación promedio de los usuarios (1-10)
     */
    @ColumnInfo(name = "average_rating")
    val averageRating: Float = 0f,
    /**
     * ID del grupo al que pertenece esta opinión
     */
    @ColumnInfo(name = "group_id")
    val groupId: String? = null,

    @ColumnInfo(name = "username")
    val username: String? = null,

    /**
     * Indica si es una película (true) o serie (false)
     */
    @ColumnInfo(name = "is_movie")
    val isMovie: Boolean = true,
) : JavaSerializable
