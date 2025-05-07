package com.bebi.watchit.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bebi.watchit.data.dao.MediaOpinionDao
import com.bebi.watchit.data.dao.SavedRecommendationDao
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.model.SavedRecommendation

@Database(
    entities = [
        MediaOpinion::class,
        SavedRecommendation::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val mediaOpinionDao: MediaOpinionDao
    abstract val savedRecommendationDao: SavedRecommendationDao

    companion object {
        const val DATABASE_NAME = "app_database.db"
    }
}