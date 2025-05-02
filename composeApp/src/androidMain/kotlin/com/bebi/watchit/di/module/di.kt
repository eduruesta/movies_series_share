package com.bebi.watchit.di.module

import com.bebi.watchit.data.database.DatabaseFactory
import com.bebi.watchit.data.domain.Localization
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Android-specific Koin module
 */
actual val nativeModule = module {
    single { DatabaseFactory(androidApplication()) }
    single<Localization> { Localization(context = androidApplication()) }
}