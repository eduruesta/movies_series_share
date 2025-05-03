package com.bebi.watchit.data.models

import com.bebi.watchit.data.JavaSerializable
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
): JavaSerializable

@Serializable
data class CriticsRequest(
    val title: String,
    val review: String,
    val score: Double,
    val author: String
): JavaSerializable
