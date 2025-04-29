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
        val isAlreadySaved = savedRecommendationDao.isRecommendationSaved(opinion.id)

        if (isAlreadySaved) {
            return false
        }

        val savedRecommendation = SavedRecommendation(
            opinionId = opinion.id,
            title = opinion.title,
            posterUrl = opinion.posterUrl,
            rating = if (opinion.averageRating > 0) opinion.averageRating else opinion.rating,
            genre = opinion.genre,
            backdropUrl = opinion.backdropUrl,
            overview = opinion.synopsis
        )

        val insertId = savedRecommendationDao.insertSavedRecommendation(savedRecommendation)
        return insertId > 0
    }


    /**
     * Elimina una recomendación guardada
     */
    suspend fun removeSavedRecommendation(opinionId: Long): Boolean {
        val isAlreadySaved = savedRecommendationDao.isRecommendationSaved(opinionId)

        if (!isAlreadySaved) {
            return false
        }

        savedRecommendationDao.deleteSavedRecommendationById(opinionId)
        return true
    }

    /**
     * Verifica si una opinión está guardada como recomendación
     */
    suspend fun isRecommendationSaved(opinionId: Long): Boolean {
        return savedRecommendationDao.isRecommendationSaved(opinionId)
    }

    /**
     * Obtiene una recomendación guardada por su ID
     */
    suspend fun getSavedRecommendationById(opinionId: Long): SavedRecommendation? {
        return savedRecommendationDao.getSavedRecommendationById(opinionId)
    }
}
