package com.bebi.watchit.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GroupResponse(
    val id: String,
    val name: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long,
    val members: List<String>,
    val inviteCode: String
)

@Serializable
data class GroupRequest(
    val name: String,
    val description: String,
    val createdBy: String
)

@Serializable
data class JoinGroupRequest(
    val memberName: String
)
