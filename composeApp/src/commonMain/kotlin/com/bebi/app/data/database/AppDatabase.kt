package com.bebi.app.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.bebi.app.data.dao.MediaOpinionDao
import com.bebi.app.data.dao.SavedRecommendationDao
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation

@Database(
    entities = [
        MediaOpinion::class,
        SavedRecommendation::class
    ],
    version = 2,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val mediaOpinionDao: MediaOpinionDao
    abstract val savedRecommendationDao: SavedRecommendationDao

    companion object {
        const val DATABASE_NAME = "app_database.db"
    }
}