package com.bebi.app.data.repository

import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de opiniones de medios
 */
interface MediaOpinionRepository {
    
    /**
     * Obtiene todas las opiniones
     */
    suspend fun getAllOpinions(): Flow<List<MediaOpinion>>
    
    /**
     * Guarda una opinión
     * @return El ID de la opinión guardada
     */
    suspend fun saveOpinion(opinion: MediaOpinion): Long
    
    /**
     * Obtiene una opinión por su ID
     */
    suspend fun getOpinionById(id: Long): Flow<MediaOpinion?>
    
    /**
     * Elimina una opinión
     */
    suspend fun deleteOpinion(opinion: MediaOpinion)
}
