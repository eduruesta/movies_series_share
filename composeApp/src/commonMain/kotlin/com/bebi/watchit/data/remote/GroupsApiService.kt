package com.bebi.watchit.data.remote

import com.bebi.watchit.data.models.CriticsRequest
import com.bebi.watchit.data.models.CriticsResponse
import com.bebi.watchit.data.models.GroupRequest
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.data.models.JoinGroupRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GroupsApiService(private val httpClient: HttpClient) {
    private val baseUrl = "https://movies-series-share-backend.onrender.com"


    // Grupos
    suspend fun getGroups(): List<GroupResponse> {
        return httpClient.get("$baseUrl/groups").body()
    }
    
    suspend fun getGroupsByMemberId(memberId: String): List<GroupResponse> {
        return httpClient.get("$baseUrl/groups/by-member/$memberId").body()
    }
    
    suspend fun createGroup(groupRequest: GroupRequest): GroupResponse {
        return httpClient.post("$baseUrl/groups") {
            contentType(ContentType.Application.Json)
            setBody(groupRequest)
        }.body()
    }
    
    suspend fun getGroupById(groupId: String): GroupResponse {
        return httpClient.get("$baseUrl/groups/$groupId").body()
    }
    
    suspend fun deleteGroup(groupId: String, userId: String): Boolean {
        return httpClient.delete("$baseUrl/groups/$groupId?userId=$userId").body()
    }
    
    suspend fun leaveGroup(groupId: String, userId: String): Boolean {
        return httpClient.delete("$baseUrl/groups/$groupId/leave?userId=$userId").body()
    }
    
    suspend fun joinGroup(inviteCode: String, joinRequest: JoinGroupRequest): GroupResponse {
        return httpClient.post("$baseUrl/groups/join/$inviteCode") {
            contentType(ContentType.Application.Json)
            setBody(joinRequest)
        }.body()
    }
    
    // Críticas dentro de grupos
    suspend fun getGroupCritics(groupId: String): List<CriticsResponse> {
        return httpClient.get("$baseUrl/groups/$groupId/critics").body()
    }
    
    suspend fun createGroupCritic(groupId: String, criticsRequest: CriticsRequest): CriticsResponse {
        return httpClient.post("$baseUrl/groups/$groupId/critics") {
            contentType(ContentType.Application.Json)
            setBody(criticsRequest)
        }.body()
    }
}
