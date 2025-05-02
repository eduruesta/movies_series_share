package com.bebi.watchit.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelos para la respuesta de proveedores de streaming (donde ver) de TMDB
 */
@Serializable
data class TmdbWatchProvidersResponse(
    @SerialName("id") val id: Int,
    @SerialName("results") val results: Map<String, CountryProviders> = emptyMap()
)

@Serializable
data class CountryProviders(
    @SerialName("link") val link: String? = null,
    @SerialName("flatrate") val flatrate: List<Provider>? = null,
    @SerialName("rent") val rent: List<Provider>? = null,
    @SerialName("buy") val buy: List<Provider>? = null
)

@Serializable
data class Provider(
    @SerialName("logo_path") val logoPath: String,
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("display_priority") val displayPriority: Int
)
