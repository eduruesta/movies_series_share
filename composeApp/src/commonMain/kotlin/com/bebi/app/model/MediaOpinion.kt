package com.bebi.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * Model class for storing movie or series opinions
 */
@Serializable
@Entity(tableName = "media_opinions",
    indices = [Index(value = ["title"], unique = true)])
data class MediaOpinion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    @ColumnInfo(name = "comment")
    val comment: String = "",
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
    val posterUrl: String? = null
)
