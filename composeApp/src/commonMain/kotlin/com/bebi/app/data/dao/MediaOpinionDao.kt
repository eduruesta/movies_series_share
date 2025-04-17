package com.bebi.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaOpinionDao {
    @Query("SELECT * FROM media_opinions ORDER BY id DESC")
    fun getAllOpinions(): Flow<List<MediaOpinion>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpinion(opinion: MediaOpinion): Long
    
    @Delete
    suspend fun deleteOpinion(opinion: MediaOpinion)
    
    @Query("SELECT * FROM media_opinions WHERE id = :id")
    suspend fun getOpinionById(id: Long): MediaOpinion?
}
