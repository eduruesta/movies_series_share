package com.bebi.watchit.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bebi.watchit.data.dao.MediaOpinionDao
import com.bebi.watchit.data.dao.SavedRecommendationDao
import com.bebi.watchit.data.dao.TmdbCacheDao
import com.bebi.watchit.data.dao.GroupCriticsCacheDao
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.model.SavedRecommendation
import com.bebi.watchit.model.TmdbCachedMedia
import com.bebi.watchit.model.GroupCriticCached

@Database(
    entities = [
        MediaOpinion::class,
        SavedRecommendation::class,
        TmdbCachedMedia::class,
        GroupCriticCached::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val mediaOpinionDao: MediaOpinionDao
    abstract val savedRecommendationDao: SavedRecommendationDao
    abstract val tmdbCacheDao: TmdbCacheDao
    abstract val groupCriticsCacheDao: GroupCriticsCacheDao

    companion object {
        const val DATABASE_NAME = "app_database.db"
    }
}