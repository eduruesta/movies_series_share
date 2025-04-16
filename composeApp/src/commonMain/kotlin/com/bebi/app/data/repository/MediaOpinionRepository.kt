package com.bebi.app.data.repository

import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing media opinions
 */
interface MediaOpinionRepository {
    /**
     * Get all media opinions as a Flow
     */
    fun getAllOpinions(): Flow<List<MediaOpinion>>
    
    /**
     * Save a new media opinion
     */
    suspend fun saveOpinion(opinion: MediaOpinion)
    
    /**
     * Delete a media opinion
     */
    suspend fun deleteOpinion(opinion: MediaOpinion)
}

/**
 * In-memory implementation of MediaOpinionRepository
 * This will be replaced with a Room implementation in the future
 */
class InMemoryMediaOpinionRepository : MediaOpinionRepository {
    private val opinions = MutableStateFlow<List<MediaOpinion>>(emptyList())
    
    override fun getAllOpinions(): Flow<List<MediaOpinion>> = opinions.asStateFlow()
    
    override suspend fun saveOpinion(opinion: MediaOpinion) {
        opinions.value = opinions.value + opinion
    }
    
    override suspend fun deleteOpinion(opinion: MediaOpinion) {
        opinions.value = opinions.value.filter { it != opinion }
    }
}
