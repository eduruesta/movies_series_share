package com.bebi.watchit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bebi.watchit.model.TmdbCachedMedia
import kotlinx.coroutines.flow.Flow

/**
 * DAO para manejar la caché de datos de TMDB
 */
@Dao
interface TmdbCacheDao {
    /**
     * Obtiene todos los medios cacheados para una categoría específica
     */
    @Query("SELECT * FROM tmdb_cache WHERE category = :category")
    fun getCachedMediaByCategory(category: String): Flow<List<TmdbCachedMedia>>

    /**
     * Obtiene todos los medios cacheados para una categoría de forma síncrona
     */
    @Query("SELECT * FROM tmdb_cache WHERE category = :category")
    suspend fun getCachedMediaByCategorySync(category: String): List<TmdbCachedMedia>

    /**
     * Inserta un medio en la caché
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: TmdbCachedMedia)

    /**
     * Inserta una lista de medios en la caché
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(mediaList: List<TmdbCachedMedia>)

    /**
     * Elimina todos los medios de una categoría específica
     */
    @Query("DELETE FROM tmdb_cache WHERE category = :category")
    suspend fun clearCategoryCache(category: String)

    /**
     * Actualiza una categoría completa con nuevos datos
     */
    @Transaction
    suspend fun updateCategoryCache(category: String, mediaList: List<TmdbCachedMedia>) {
        clearCategoryCache(category)
        insertAllMedia(mediaList)
    }

    /**
     * Verifica si existe caché para una categoría
     */
    @Query("SELECT COUNT(*) FROM tmdb_cache WHERE category = :category")
    suspend fun hasCacheForCategory(category: String): Int

    /**
     * Obtiene un medio específico por su ID
     */
    @Query("SELECT * FROM tmdb_cache WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: Long): TmdbCachedMedia?

}
