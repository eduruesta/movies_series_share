package com.bebi.watchit.di.module

import com.bebi.watchit.data.database.DatabaseFactory
import org.koin.dsl.module

actual val nativeModule = module {
    single { DatabaseFactory() }
}