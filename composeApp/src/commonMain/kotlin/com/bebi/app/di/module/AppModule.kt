package com.bebi.app.di.module

import com.bebi.app.data.database.AppDatabase
import com.bebi.app.data.repository.RoomMediaOpinionRepository
import com.bebi.app.viewmodel.MediaDetailViewModel
import com.bebi.app.viewmodel.MediaOpinionViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Common Koin module for the application
 */
val appModule = module {
    // Database
    single { get<DatabaseProvider>().getDatabase() }
    single { get<AppDatabase>().mediaOpinionDao }

    // Repositories
    factoryOf(::RoomMediaOpinionRepository)
}

val viewModelModule = module {
    factoryOf(::MediaOpinionViewModel)
    factoryOf(::MediaDetailViewModel)
}

expect interface DatabaseProvider {
    fun getDatabase(): AppDatabase
}

expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, nativeModule, viewModelModule)
    }
}
