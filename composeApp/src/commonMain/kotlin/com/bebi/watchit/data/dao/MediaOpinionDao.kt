package com.bebi.watchit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bebi.watchit.model.MediaOpinion
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
    fun getOpinionById(id: Long): Flow<MediaOpinion?>
}
