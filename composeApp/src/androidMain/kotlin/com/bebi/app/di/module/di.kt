package com.bebi.app.di.module

import com.bebi.app.data.database.DatabaseFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
actual val nativeModule = module {
    single { DatabaseFactory(androidApplication()) }
}