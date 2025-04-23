package com.bebi.app.data.repository

import com.bebi.app.data.dao.MediaOpinionDao
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Room implementation of MediaOpinionRepository
 * Uses Room database for persistent storage
 */
class RoomMediaOpinionRepository(
    private val mediaOpinionDao: MediaOpinionDao
) : MediaOpinionRepository {

    override suspend fun getAllOpinions(): Flow<List<MediaOpinion>> {
        return mediaOpinionDao.getAllOpinions()
    }

    override suspend fun saveOpinion(opinion: MediaOpinion): Long {
        return mediaOpinionDao.insertOpinion(opinion)
    }

    override suspend fun getOpinionById(id: Long): Flow<MediaOpinion?> {
        return mediaOpinionDao.getOpinionById(id)
    }
    
    override suspend fun getOpinionByIdDirect(id: Long): MediaOpinion? {
        return mediaOpinionDao.getOpinionById(id).firstOrNull()
    }
    
    override suspend fun updateOpinionById(id: Long, opinion: MediaOpinion): Boolean {
        val opinionToUpdate = opinion.copy(id = id)
        val result = mediaOpinionDao.insertOpinion(opinionToUpdate)
        return result > 0
    }

    override suspend fun deleteOpinion(opinion: MediaOpinion) {
        mediaOpinionDao.deleteOpinion(opinion)
    }
}
