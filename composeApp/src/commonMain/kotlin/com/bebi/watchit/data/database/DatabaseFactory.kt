package com.bebi.watchit.data.database

import androidx.room.RoomDatabase

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DatabaseFactory {
    fun createAppDatabase(): RoomDatabase.Builder<AppDatabase>
}