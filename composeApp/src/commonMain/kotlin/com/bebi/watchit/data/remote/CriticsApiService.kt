package com.bebi.watchit.data.remote

import com.bebi.watchit.model.MediaOpinion
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Servicio para comunicarse con el backend de críticas
 */
class CriticsApiService(private val httpClient: HttpClient) {
    
    private val baseUrl = "https://movies-series-share-backend.onrender.com/critics"
    
    /**
     * Obtiene todas las críticas desde el backend
     */
    suspend fun getAllCritics(): Result<List<MediaOpinion>> = withContext(Dispatchers.IO) {
        try {
            val response: List<MediaOpinion> = httpClient.get(baseUrl).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Guarda una nueva crítica en el backend
     */
    suspend fun saveCritic(opinion: MediaOpinion): Result<MediaOpinion> = withContext(Dispatchers.IO) {
        try {
            val response: MediaOpinion = httpClient.post(baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(opinion)
            }.body()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene una crítica por su ID
     */
    suspend fun getCriticById(id: Long): Result<MediaOpinion> = withContext(Dispatchers.IO) {
        try {
            val response: MediaOpinion = httpClient.get("$baseUrl/$id").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza una crítica existente por su ID
     */
    suspend fun updateCritic(id: Long, opinion: MediaOpinion): Result<MediaOpinion> = withContext(Dispatchers.IO) {
        try {
            val response: MediaOpinion = httpClient.put("$baseUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(opinion)
            }.body()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
