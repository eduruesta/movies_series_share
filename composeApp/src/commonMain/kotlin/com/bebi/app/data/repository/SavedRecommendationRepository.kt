package com.bebi.app.data.repository

import com.bebi.app.data.dao.SavedRecommendationDao
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar las recomendaciones guardadas
 */
class SavedRecommendationRepository(
    private val savedRecommendationDao: SavedRecommendationDao
) {
    /**
     * Obtiene todas las recomendaciones guardadas
     */
    fun getAllSavedRecommendations(): Flow<List<SavedRecommendation>> {
        return savedRecommendationDao.getAllSavedRecommendations()
    }
    
    /**
     * Guarda una opinión como recomendación
     * @return true si se guardó correctamente, false si ya estaba guardada
     */
    suspend fun saveRecommendation(opinion: MediaOpinion): Boolean {
        // Primero verificamos si ya está guardada
        val isAlreadySaved = savedRecommendationDao.isRecommendationSaved(opinion.id)
        
        // Si ya está guardada, no hacemos nada
        if (isAlreadySaved) {
            return false
        }
        
        // Transformamos la opinión a recomendación guardada
        val savedRecommendation = SavedRecommendation(
            opinionId = opinion.id,
            title = opinion.title,
            posterUrl = opinion.posterUrl,
            rating = opinion.rating, // Usamos la calificación original, no el promedio
            genre = opinion.genre,
            backdropUrl = opinion.backdropUrl
        )
        
        // Guardamos la recomendación
        val insertId = savedRecommendationDao.insertSavedRecommendation(savedRecommendation)
        return insertId > 0
    }
    
    /**
     * Guarda una recomendación basada en IDs
     * @return true si se guardó correctamente, false si ya estaba guardada
     */
    suspend fun saveRecommendation(title: String, mediaId: String, opinionId: Long): Boolean {
        // Primero verificamos si ya está guardada
        val isAlreadySaved = savedRecommendationDao.isRecommendationSaved(opinionId)
        
        // Si ya está guardada, no hacemos nada
        if (isAlreadySaved) {
            return false
        }
        
        // Creamos una recomendación guardada con los datos mínimos
        val savedRecommendation = SavedRecommendation(
            opinionId = opinionId,
            title = title,
            posterUrl = null,
            rating = 0.0f,  // Usar Float, no Double
            genre = "",     // Usar string vacío, no null
            backdropUrl = null
        )
        
        // Guardamos la recomendación
        val insertId = savedRecommendationDao.insertSavedRecommendation(savedRecommendation)
        return insertId > 0
    }
    
    /**
     * Elimina una recomendación guardada
     */
    suspend fun removeSavedRecommendation(opinionId: Long): Boolean {
        // Verificamos si está guardada
        val isAlreadySaved = savedRecommendationDao.isRecommendationSaved(opinionId)
        
        // Si no está guardada, no hacemos nada
        if (!isAlreadySaved) {
            return false
        }
        
        // Eliminamos la recomendación
        savedRecommendationDao.deleteSavedRecommendationById(opinionId)
        return true
    }
    
    /**
     * Verifica si una opinión está guardada como recomendación
     */
    suspend fun isRecommendationSaved(opinionId: Long): Boolean {
        return savedRecommendationDao.isRecommendationSaved(opinionId)
    }
}
