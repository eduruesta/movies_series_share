package com.bebi.app.di.module

import com.bebi.app.data.database.DatabaseFactory
import org.koin.dsl.module

actual val nativeModule = module {
    single { DatabaseFactory() }
}