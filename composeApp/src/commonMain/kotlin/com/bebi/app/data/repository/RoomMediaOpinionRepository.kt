package com.bebi.app.data.repository

import com.bebi.app.data.dao.MediaOpinionDao
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow

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

    override suspend fun deleteOpinion(opinion: MediaOpinion) {
        mediaOpinionDao.deleteOpinion(opinion)
    }
}
