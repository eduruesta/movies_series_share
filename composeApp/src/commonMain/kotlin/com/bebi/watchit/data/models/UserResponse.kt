package com.bebi.watchit.data.models

import com.bebi.watchit.data.JavaSerializable
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String
) : JavaSerializable

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        name = name,
        email = email
    )
}
