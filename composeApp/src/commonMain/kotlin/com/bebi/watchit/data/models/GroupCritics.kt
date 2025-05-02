package com.bebi.watchit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CriticsResponse(
    val id: String? = null,
    val title: String,
    val review: String,
    val score: Double,
    val author: String,
    val groupId: String? = null,
    val createdAt: Long? = null
)

@Serializable
data class CriticsRequest(
    val title: String,
    val review: String,
    val score: Double,
    val author: String
)
