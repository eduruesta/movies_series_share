package com.bebi.watchit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bebi.watchit.model.GroupCriticCached
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.Flow

/**
 * DAO para manejar la caché de críticas de grupos
 */
@Dao
interface GroupCriticsCacheDao {
    /**
     * Obtiene todas las críticas en caché para un usuario específico
     */
    @Query("SELECT * FROM group_critics_cache WHERE userId = :userId ORDER BY lastUpdated DESC")
    fun getCachedCriticsByUserId(userId: String): Flow<List<GroupCriticCached>>

    /**
     * Inserta una crítica en la caché
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCritic(critic: GroupCriticCached)

    /**
     * Inserta una lista de críticas en la caché
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCritics(critics: List<GroupCriticCached>)

    /**
     * Elimina todas las críticas de un usuario específico
     */
    @Query("DELETE FROM group_critics_cache WHERE userId = :userId")
    suspend fun clearUserCache(userId: String)

    /**
     * Actualiza la caché de un usuario con nuevos datos
     */
    @Transaction
    suspend fun updateUserCache(userId: String, critics: List<GroupCriticCached>) {
        clearUserCache(userId)
        insertAllCritics(critics)
    }

    /**
     * Verifica si existe caché para un usuario
     */
    @Query("SELECT COUNT(*) FROM group_critics_cache WHERE userId = :userId")
    suspend fun hasCacheForUser(userId: String): Int

    /**
     * Convierte los datos cacheados a MediaOpinion
     */
    fun GroupCriticCached.toMediaOpinion(): MediaOpinion {
        return MediaOpinion(
            id = this.id,
            title = this.title,
            posterUrl = this.posterUrl,
            backdropUrl = this.backdropUrl,
            genre = this.genre,
            platform = this.platform,
            rating = this.rating,
            averageRating = this.averageRating,
            synopsis = this.synopsis,
            year = this.year
        )
    }
}
