package com.bebi.app.di.module

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bebi.app.data.database.AppDatabase
import com.bebi.app.data.database.DatabaseFactory
import com.bebi.app.data.repository.RoomMediaOpinionRepository
import com.bebi.app.viewmodel.MediaDetailViewModel
import com.bebi.app.viewmodel.MediaOpinionViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Common Koin module for the application
 */

val appModule = module {
    // Database
    single {
        get<DatabaseFactory>()
            .createAppDatabase()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().mediaOpinionDao }
}

val dataModule = module {
    factoryOf(::RoomMediaOpinionRepository)
}

val viewModelModule = module {
    viewModelOf(::MediaOpinionViewModel)
    viewModelOf(::MediaDetailViewModel)
}

expect val nativeModule: Module


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule, nativeModule)
    }
}
