package com.bebi.app.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.bebi.app.data.dao.MediaOpinionDao
import com.bebi.app.model.MediaOpinion

@Database(entities = [MediaOpinion::class], version = 1, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val mediaOpinionDao: MediaOpinionDao

    companion object {
        const val DATABASE_NAME = "app_database.db"
    }
}