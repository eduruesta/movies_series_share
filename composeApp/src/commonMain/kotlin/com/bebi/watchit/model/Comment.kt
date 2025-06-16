package com.bebi.watchit.model

import kotlinx.serialization.Serializable
import com.bebi.watchit.data.JavaSerializable

/**
 * Representa un comentario hecho por un usuario sobre una opinión de media.
 * Esta clase se utiliza tanto para la persistencia local (Room) como para la comunicación con el backend.
 */
@Serializable
data class Comment(
    val text: String = "",
    val username: String = "",
) : JavaSerializable
