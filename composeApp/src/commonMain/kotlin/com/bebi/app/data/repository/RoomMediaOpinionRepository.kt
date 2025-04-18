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
) {

    fun getAllOpinions(): Flow<List<MediaOpinion>> {
        return mediaOpinionDao.getAllOpinions()
    }

    suspend fun saveOpinion(opinion: MediaOpinion) {
        mediaOpinionDao.insertOpinion(opinion)
    }

    suspend fun deleteOpinion(opinion: MediaOpinion) {
        mediaOpinionDao.deleteOpinion(opinion)
    }
}
