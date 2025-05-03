package com.bebi.watchit.di.module

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bebi.app.watchit.BuildConfig
import com.bebi.watchit.data.database.AppDatabase
import com.bebi.watchit.data.database.DatabaseFactory
import com.bebi.watchit.data.remote.AppService
import com.bebi.watchit.data.remote.CriticsApiService
import com.bebi.watchit.data.repository.HybridMediaOpinionRepository
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.data.repository.RoomMediaOpinionRepository
import com.bebi.watchit.data.repository.SavedRecommendationRepository
import com.bebi.watchit.data.repository.TmdbRepository
import com.bebi.watchit.viewmodel.MediaDetailViewModel
import com.bebi.watchit.viewmodel.MediaOpinionFormViewModel
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import com.bebi.watchit.viewmodel.SavedRecommendationViewModel
import com.bebi.watchit.viewmodel.TopMoviesViewModel
import com.bebi.watchit.viewmodel.TopSeriesViewModel
import com.bebi.watchit.viewmodel.TrendingMoviesViewModel
import com.bebi.watchit.viewmodel.TrendingSeriesViewModel
import com.bebi.watchit.viewmodel.UpcomingMoviesViewModel
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
import com.bebi.watchit.data.remote.GroupsApiService
import com.bebi.watchit.data.repository.GroupsRepository
import com.bebi.watchit.data.repository.GroupsRepositoryImpl
import com.bebi.watchit.viewmodel.GroupCriticsViewModel
import com.bebi.watchit.viewmodel.GroupsViewModel
import com.bebi.watchit.viewmodel.GroupDetailViewModel

/**
 * Common Koin module for the application
 */

val appModule = module {
    single(named("apiKey")) { BuildConfig.api_key }

    single {
        get<DatabaseFactory>()
            .createAppDatabase()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDatabase>().mediaOpinionDao }
    single { get<AppDatabase>().savedRecommendationDao }

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
    factoryOf(::CriticsApiService)
    factoryOf(::GroupsApiService)

    factory<MediaOpinionRepository> {
        HybridMediaOpinionRepository(
            apiService = get(),
            localRepository = get<RoomMediaOpinionRepository>(),
            savedRecommendationRepository = get<SavedRecommendationRepository>()
        )
    }

    single<GroupsRepository> {
        GroupsRepositoryImpl(
            apiService = get<GroupsApiService>()
        )
    }
    factoryOf(::SavedRecommendationRepository)

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
    viewModelOf(::SavedRecommendationViewModel)
    viewModelOf(::GroupDetailViewModel)

    // ViewModels para las pantallas de TMDB - ahora necesitan MediaOpinionRepository
    factory { TopSeriesViewModel(get(), get()) }
    factory { TrendingSeriesViewModel(get(), get()) }
    factory { UpcomingMoviesViewModel(get(), get()) }
    factory { TopMoviesViewModel(get(), get()) }
    factory { TrendingMoviesViewModel(get(), get()) }
    factory { (username: String) ->
        GroupsViewModel(
            groupsRepository = get(),
            currentUsername = username
        )
    }

    factory { (username: String, groupId: String) ->
        GroupCriticsViewModel(
            groupsRepository = get(),
            currentUsername = username,
            groupId = groupId
        )
    }
}

expect val nativeModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            appModule,
            dataModule,
            viewModelModule,
            nativeModule,
        )
    }
}
