package com.bebi.app.di

import com.bebi.app.data.repository.InMemoryMediaOpinionRepository
import com.bebi.app.data.repository.MediaOpinionRepository
import com.bebi.app.viewmodel.MediaOpinionViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for the application
 */
val appModule = module {
    // Repositories
    singleOf(::InMemoryMediaOpinionRepository) bind MediaOpinionRepository::class
    
    // ViewModels
    factoryOf(::MediaOpinionViewModel)
}
