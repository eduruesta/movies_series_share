package com.bebi.watchit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Model class for storing saved recommendations
 */
@Serializable
@Entity(tableName = "saved_recommendations")
data class SavedRecommendation(
    @PrimaryKey
    @ColumnInfo(name = "opinion_id")
    val opinionId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "poster_url")
    val posterUrl: String?,

    @ColumnInfo(name = "backdrop_url")
    val backdropUrl: String?,

    @ColumnInfo(name = "saved_timestamp")
    val savedTimestamp: Long = Clock.System.now().toEpochMilliseconds(),

    @ColumnInfo(name = "rating")
    val rating: Float,

    @ColumnInfo(name = "genre")
    val genre: String,

    @ColumnInfo(name = "overview")
    val overview: String
)
