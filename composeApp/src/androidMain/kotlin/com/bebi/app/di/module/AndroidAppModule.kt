package com.bebi.app.di.module

import com.bebi.app.data.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
actual val nativeModule = module {
    single<DatabaseProvider> { AndroidDatabaseProvider(androidApplication()) }
}

class AndroidDatabaseProvider(private val context: android.content.Context) : DatabaseProvider {
    override fun getDatabase(): AppDatabase {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(DATABASE_NAME)
        return androidx.room.Room.databaseBuilder(
            appContext, 
            AppDatabase::class.java,
            dbFile.absolutePath
        ).build()
    }
    
    companion object {
        const val DATABASE_NAME = "media_opinions_database"
    }
}

actual interface DatabaseProvider {
    actual fun getDatabase(): AppDatabase
}
