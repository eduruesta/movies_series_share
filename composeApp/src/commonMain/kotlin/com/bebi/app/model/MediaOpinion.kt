package com.bebi.app.model

/**
 * Model class for storing movie or series opinions
 */
data class MediaOpinion(
    val title: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val imageUrl: String = "",
    val genre: String = "",
    val platform: String = ""
)
