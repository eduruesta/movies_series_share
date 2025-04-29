package com.bebi.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bebi.app.model.SavedRecommendation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecommendationDao {
    /**
     * Obtiene todas las recomendaciones guardadas ordenadas por fecha de guardado
     */
    @Query("SELECT * FROM saved_recommendations ORDER BY saved_timestamp DESC")
    fun getAllSavedRecommendations(): Flow<List<SavedRecommendation>>
    
    /**
     * Inserta una recomendación, o la reemplaza si ya existe
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedRecommendation(recommendation: SavedRecommendation): Long
    
    /**
     * Elimina una recomendación
     */
    @Delete
    suspend fun deleteSavedRecommendation(recommendation: SavedRecommendation)
    
    /**
     * Verifica si una opinión está guardada como recomendación
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_recommendations WHERE opinion_id = :opinionId LIMIT 1)")
    suspend fun isRecommendationSaved(opinionId: Long): Boolean
    
    /**
     * Obtiene una recomendación guardada por su ID de opinión
     */
    @Query("SELECT * FROM saved_recommendations WHERE opinion_id = :opinionId LIMIT 1")
    suspend fun getSavedRecommendationById(opinionId: Long): SavedRecommendation?
    
    /**
     * Elimina una recomendación por su ID de opinión
     */
    @Query("DELETE FROM saved_recommendations WHERE opinion_id = :opinionId")
    suspend fun deleteSavedRecommendationById(opinionId: Long)
}
