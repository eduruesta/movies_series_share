package com.bebi.watchit.data.repository

import com.bebi.watchit.data.models.CriticsRequest
import com.bebi.watchit.data.models.CriticsResponse
import com.bebi.watchit.data.models.GroupRequest
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.data.models.JoinGroupRequest
import com.bebi.watchit.data.remote.GroupsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface GroupsRepository {
    suspend fun getGroups(): Result<List<GroupResponse>>
    suspend fun getGroupsByMemberId(memberId: String): Result<List<GroupResponse>>
    suspend fun createGroup(name: String, description: String, userId: String, userName: String, userEmail: String): Result<GroupResponse>
    suspend fun getGroupById(groupId: String): Result<GroupResponse>
    suspend fun joinGroup(inviteCode: String, userId: String, userName: String, userEmail: String): Result<GroupResponse>
    suspend fun getGroupCritics(groupId: String): Result<List<CriticsResponse>>
    suspend fun createGroupCritic(groupId: String, title: String, review: String, score: Double, author: String): Result<CriticsResponse>
    suspend fun deleteGroup(groupId: String, userId: String): Result<Boolean>
    suspend fun leaveGroup(groupId: String, userId: String): Result<Boolean>
}

class GroupsRepositoryImpl(private val apiService: GroupsApiService) : GroupsRepository {
    
    override suspend fun getGroups(): Result<List<GroupResponse>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getGroups())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupsByMemberId(memberId: String): Result<List<GroupResponse>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getGroupsByMemberId(memberId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createGroup(name: String, description: String, userId: String, userName: String, userEmail: String): Result<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val groupRequest = GroupRequest(name, description, userId, userName, userEmail)
            Result.success(apiService.createGroup(groupRequest))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupById(groupId: String): Result<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getGroupById(groupId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun joinGroup(inviteCode: String, userId: String, userName: String, userEmail: String): Result<GroupResponse> = withContext(Dispatchers.IO) {
        try {
            val joinRequest = JoinGroupRequest(userId, userName, userEmail)
            Result.success(apiService.joinGroup(inviteCode, joinRequest))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupCritics(groupId: String): Result<List<CriticsResponse>> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getGroupCritics(groupId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createGroupCritic(groupId: String, title: String, review: String, score: Double, author: String): Result<CriticsResponse> = withContext(Dispatchers.IO) {
        try {
            val criticsRequest = CriticsRequest(title, review, score, author)
            Result.success(apiService.createGroupCritic(groupId, criticsRequest))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteGroup(groupId: String, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.deleteGroup(groupId, userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun leaveGroup(groupId: String, userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.leaveGroup(groupId, userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
