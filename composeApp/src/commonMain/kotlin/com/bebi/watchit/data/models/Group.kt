package com.bebi.watchit.data.models

import com.bebi.watchit.data.JavaSerializable
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
): JavaSerializable

@Serializable
data class GroupRequest(
    val name: String,
    val description: String,
    val createdBy: String
): JavaSerializable

@Serializable
data class JoinGroupRequest(
    val memberName: String
): JavaSerializable
