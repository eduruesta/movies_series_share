package com.bebi.app.di.module

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bebi.app.BuildConfig
import com.bebi.app.data.database.AppDatabase
import com.bebi.app.data.database.DatabaseFactory
import com.bebi.app.data.remote.AppService
import com.bebi.app.data.repository.RoomMediaOpinionRepository
import com.bebi.app.data.repository.TmdbRepository
import com.bebi.app.viewmodel.MediaDetailViewModel
import com.bebi.app.viewmodel.MediaOpinionFormViewModel
import com.bebi.app.viewmodel.MediaOpinionViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Common Koin module for the application
 */

val appModule = module {
    single(named("apiKey")) { BuildConfig.api_key }

    // Database
    single {
        get<DatabaseFactory>()
            .createAppDatabase()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().mediaOpinionDao }

    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 40000
                connectTimeoutMillis = 40000
                socketTimeoutMillis = 40000
            }
        }

    }
}

val dataModule = module {
    factoryOf(::RoomMediaOpinionRepository)
    // TMDB API
    singleOf(::TmdbRepository)
    factoryOf(::AppService)
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 40000
                connectTimeoutMillis = 40000
                socketTimeoutMillis = 40000
            }
        }

    }
}

val viewModelModule = module {
    viewModelOf(::MediaOpinionViewModel)
    viewModelOf(::MediaDetailViewModel)
    viewModelOf(::MediaOpinionFormViewModel)
}

expect val nativeModule: Module


fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule, dataModule, viewModelModule, nativeModule)
    }
}
