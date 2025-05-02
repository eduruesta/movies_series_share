package com.bebi.watchit.di.module

import com.bebi.watchit.data.database.DatabaseFactory
import com.bebi.watchit.data.domain.Localization
import org.koin.dsl.module

actual val nativeModule = module {
    single { DatabaseFactory() }
    single<Localization> { Localization() }
}