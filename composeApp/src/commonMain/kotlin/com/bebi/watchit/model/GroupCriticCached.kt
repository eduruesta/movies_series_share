package com.bebi.watchit.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * Entidad para almacenar las críticas de grupos en caché local
 */
@Entity(tableName = "group_critics_cache")
@Serializable
data class GroupCriticCached(
    @PrimaryKey
    val id: Long,
    val title: String,
    val posterUrl: String?, // URL completa del poster
    val backdropUrl: String?, // URL completa del backdrop
    val posterPath: String?, // Ruta parcial del poster (por si se necesita reconstruir)
    val backdropPath: String?, // Ruta parcial del backdrop (por si se necesita reconstruir)
    val genre: String,
    val platform: String,
    val rating: Float,
    val averageRating: Float,
    val synopsis: String,
    val year: String,
    val userId: String, // Usuario al que pertenece esta crítica en caché
    val lastUpdated: Long = Clock.System.now().toEpochMilliseconds()
)
