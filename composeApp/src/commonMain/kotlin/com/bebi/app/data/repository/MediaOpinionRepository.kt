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
     * Obtiene una opinión por su ID utilizando Flow
     */
    suspend fun getOpinionById(id: Long): Flow<MediaOpinion?>
    
    /**
     * Obtiene directamente una opinión por su ID (sin Flow)
     * @return La opinión si existe, o null si no se encuentra
     */
    suspend fun getOpinionByIdDirect(id: Long): MediaOpinion?
    
    /**
     * Actualiza una opinión existente por su ID
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    suspend fun updateOpinionById(id: Long, opinion: MediaOpinion): Boolean
    
    /**
     * Elimina una opinión
     */
    suspend fun deleteOpinion(opinion: MediaOpinion)
}
