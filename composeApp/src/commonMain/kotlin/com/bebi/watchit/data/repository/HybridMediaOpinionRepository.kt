package com.bebi.watchit.data.repository

import com.bebi.watchit.data.remote.CriticsApiService
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

/**
 * Repositorio híbrido que usa la API para obtener/guardar datos y Room como fallback
 */
class HybridMediaOpinionRepository(
    private val apiService: CriticsApiService,
    private val localRepository: MediaOpinionRepository,
    private val savedRecommendationRepository: SavedRecommendationRepository
) : MediaOpinionRepository {

    /**
     * Obtiene todas las opiniones, primero intenta desde la API y si falla usa Room
     */
    override suspend fun getAllOpinions(): Flow<List<MediaOpinion>> = flow {
        val apiResult = apiService.getAllCritics()

        if (apiResult.isSuccess) {
            val opinions = apiResult.getOrNull() ?: emptyList()
            emit(opinions)

            opinions.forEach { opinion ->
                localRepository.saveOpinion(opinion)
            }
        } else {
            localRepository.getAllOpinions().collect { localOpinions ->
                emit(localOpinions)
            }
        }
    }.catch { e ->
        localRepository.getAllOpinions().collect { localOpinions ->
            emit(localOpinions)
        }
    }

    /**
     * Guarda una opinión, primero intenta en la API y si falla la guarda en Room
     */
    override suspend fun saveOpinion(opinion: MediaOpinion): Long {
        val opinionToSave = if (opinion.id <= 0) {
            val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)
            opinion.copy(id = randomId)
        } else {
            opinion
        }

        val apiResult = apiService.saveCritic(opinionToSave)

        return if (apiResult.isSuccess) {
            val savedOpinion = apiResult.getOrNull()
            if (savedOpinion != null) {
                localRepository.saveOpinion(savedOpinion)
            } else {
                localRepository.saveOpinion(opinionToSave)
            }
        } else {
            localRepository.saveOpinion(opinionToSave)
        }
    }

    /**
     * Obtiene una opinión por su ID usando Flow
     */
    override suspend fun getOpinionById(id: Long): Flow<MediaOpinion?> {
        return localRepository.getOpinionById(id)
    }

    /**
     * Obtiene directamente una opinión por su ID (sin Flow)
     */
    override suspend fun getOpinionByIdDirect(id: Long): MediaOpinion? {
        val apiResult = apiService.getCriticById(id)

        if (apiResult.isSuccess) {
            val opinion = apiResult.getOrNull()
            if (opinion?.id != null && opinion.id > 0) {
                localRepository.saveOpinion(opinion)
                return opinion
            } else {
                val localOpinion = localRepository.getOpinionByIdDirect(id)
                if (localOpinion != null) {
                    return localOpinion
                }
                
                if (savedRecommendationRepository.isRecommendationSaved(id)) {
                    val savedRec = savedRecommendationRepository.getSavedRecommendationById(id)
                    
                    if (savedRec != null) {
                        return MediaOpinion(
                            id = savedRec.opinionId,
                            title = savedRec.title,
                            comments = emptyList(), // Ahora es una lista vacía
                            genre = savedRec.genre ?: "",
                            posterUrl = savedRec.posterUrl,
                            backdropUrl = savedRec.backdropUrl,
                            averageRating = savedRec.rating,
                            synopsis = savedRec.overview ?: "",
                        )
                    }
                }
            }
        }

        return localRepository.getOpinionById(id).firstOrNull()
    }

    /**
     * Actualiza una opinión existente por su ID
     */
    override suspend fun updateOpinionById(id: Long, opinion: MediaOpinion): Boolean {
        val opinionToUpdate = opinion.copy(id = id)

        val apiResult = apiService.updateCritic(id, opinionToUpdate)

        return if (apiResult.isSuccess) {
            val updatedOpinion = apiResult.getOrNull()
            if (updatedOpinion != null) {
                localRepository.saveOpinion(updatedOpinion)
                true
            } else {
                localRepository.saveOpinion(opinionToUpdate)
                true
            }
        } else {
            localRepository.saveOpinion(opinionToUpdate)
            false
        }
    }

    /**
     * Elimina una opinión
     */
    override suspend fun deleteOpinion(opinion: MediaOpinion) {
        localRepository.deleteOpinion(opinion)
    }
    
    /**
     * Obtiene todas las opiniones de un grupo específico
     */
    override suspend fun getOpinionsByGroupId(groupId: String): Flow<List<MediaOpinion>> = flow {
        val apiResult = apiService.getCriticsByGroupId(groupId)
        
        if (apiResult.isSuccess) {
            val groupOpinions = apiResult.getOrNull() ?: emptyList()
            emit(groupOpinions)
            
            groupOpinions.forEach { opinion ->
                localRepository.saveOpinion(opinion)
            }
        } else {
            localRepository.getOpinionsByGroupId(groupId).collect { groupOpinions ->
                emit(groupOpinions)
            }
        }
    }.catch { e ->
        localRepository.getOpinionsByGroupId(groupId).collect { groupOpinions ->
            emit(groupOpinions)
        }
    }
}
