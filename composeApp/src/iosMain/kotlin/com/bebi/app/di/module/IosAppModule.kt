package com.bebi.app.di.module

import com.bebi.app.data.database.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS-specific Koin module
 */
actual val nativeModule = module {
    single<DatabaseProvider> { IosDatabaseProvider() }
}

class IosDatabaseProvider : DatabaseProvider {
    override fun getDatabase(): AppDatabase {
        val dbFile = documentDirectory() + "/${DATABASE_NAME}"
        return androidx.room.Room.databaseBuilder<AppDatabase>(
            name = dbFile
        ).build()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(documentDirectory?.path())
    }
    
    companion object {
        const val DATABASE_NAME = "media_opinions_database"
    }
}

actual interface DatabaseProvider {
    actual fun getDatabase(): AppDatabase
}