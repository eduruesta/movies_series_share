package com.bebi.app.data.repository

import com.bebi.app.data.remote.CriticsApiService
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Repositorio híbrido que usa la API para obtener/guardar datos y Room como fallback
 */
class HybridMediaOpinionRepository(
    private val apiService: CriticsApiService,
    private val localRepository: RoomMediaOpinionRepository
) : MediaOpinionRepository {

    /**
     * Obtiene todas las opiniones, primero intenta desde la API y si falla usa Room
     */
    override suspend fun getAllOpinions(): Flow<List<MediaOpinion>> = flow {
        // Intenta obtener datos desde la API
        val apiResult = apiService.getAllCritics()
        
        if (apiResult.isSuccess) {
            // Si la API responde correctamente, emite los datos y actualiza la caché local
            val opinions = apiResult.getOrNull() ?: emptyList()
            emit(opinions)
            
            // Actualiza la caché local en segundo plano
            opinions.forEach { opinion ->
                localRepository.saveOpinion(opinion)
            }
        } else {
            // Si la API falla, usa los datos de Room
            localRepository.getAllOpinions().collect { localOpinions ->
                emit(localOpinions)
            }
        }
    }.catch { e ->
        // En caso de error, usa los datos de Room
        localRepository.getAllOpinions().collect { localOpinions ->
            emit(localOpinions)
        }
    }

    /**
     * Guarda una opinión, primero intenta en la API y si falla la guarda en Room
     */
    override suspend fun saveOpinion(opinion: MediaOpinion): Long {
        // Asegurarse de que la opinión tenga un ID válido
        val opinionToSave = if (opinion.id <= 0) {
            // Si no tiene ID o es 0, generamos uno
            val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)
            opinion.copy(id = randomId)
        } else {
            opinion
        }

        // Intenta guardar en la API
        val apiResult = apiService.saveCritic(opinionToSave)
        
        return if (apiResult.isSuccess) {
            // Si la API responde correctamente, guarda también localmente
            val savedOpinion = apiResult.getOrNull()
            if (savedOpinion != null) {
                localRepository.saveOpinion(savedOpinion)
            } else {
                localRepository.saveOpinion(opinionToSave)
            }
        } else {
            // Si la API falla, guarda solo localmente
            localRepository.saveOpinion(opinionToSave)
        }
    }

    /**
     * Obtiene una opinión por su ID usando Flow
     */
    override suspend fun getOpinionById(id: Long): Flow<MediaOpinion?> {
        // Para obtener por ID usamos directamente Room ya que la API no tiene este endpoint
        return localRepository.getOpinionById(id)
    }
    
    /**
     * Obtiene directamente una opinión por su ID (sin Flow)
     */
    override suspend fun getOpinionByIdDirect(id: Long): MediaOpinion? {
        // Intenta obtener desde la API
        val apiResult = apiService.getCriticById(id)
        
        if (apiResult.isSuccess) {
            val opinion = apiResult.getOrNull()
            // Si se encuentra en la API, actualiza la caché local y devuelve
            if (opinion != null) {
                localRepository.saveOpinion(opinion)
                return opinion
            }
        }
        
        // Si la API falla o no encuentra la opinión, busca en local
        return localRepository.getOpinionById(id).firstOrNull()
    }
    
    /**
     * Actualiza una opinión existente por su ID
     */
    override suspend fun updateOpinionById(id: Long, opinion: MediaOpinion): Boolean {
        // Asegurarse de que la opinión tenga el ID correcto
        val opinionToUpdate = opinion.copy(id = id)
        
        // Intenta actualizar en la API
        val apiResult = apiService.updateCritic(id, opinionToUpdate)
        
        return if (apiResult.isSuccess) {
            val updatedOpinion = apiResult.getOrNull()
            // Si se actualiza con éxito en la API, actualiza también localmente
            if (updatedOpinion != null) {
                localRepository.saveOpinion(updatedOpinion)
                true
            } else {
                localRepository.saveOpinion(opinionToUpdate)
                true
            }
        } else {
            // Si la API falla, actualiza solo localmente
            localRepository.saveOpinion(opinionToUpdate)
            false // Devuelve false porque no pudimos actualizar en el backend
        }
    }

    /**
     * Elimina una opinión
     */
    override suspend fun deleteOpinion(opinion: MediaOpinion) {
        // Por simplicidad, solo eliminamos de Room
        // En una implementación real, también deberíamos eliminar de la API
        localRepository.deleteOpinion(opinion)
    }
}
