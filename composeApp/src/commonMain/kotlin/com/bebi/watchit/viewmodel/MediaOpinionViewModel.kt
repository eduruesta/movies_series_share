package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.dao.GroupCriticsCacheDao
import com.bebi.watchit.data.repository.GroupsRepository
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.model.GroupCriticCached
import com.bebi.watchit.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing media opinions
 */
class MediaOpinionViewModel(
    private val repository: MediaOpinionRepository,
    private val groupsRepository: GroupsRepository,
    private var currentUserId: String,
    private val groupCriticsCacheDao: GroupCriticsCacheDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaOpinionUiState())
    val uiState: StateFlow<MediaOpinionUiState> = _uiState.asStateFlow()


    /**
     * Actualiza el ID del usuario actual y limpia los datos anteriores
     */
    fun updateCurrentUser(userId: String) {
        if (currentUserId != userId) {
            currentUserId = userId
            _uiState.update {
                it.copy(
                    groupCritics = emptyList(),
                    isLoadingGroupCritics = false
                )
            }
        }
    }
    
    /**
     * Limpia todos los datos almacenados en el ViewModel
     */
    fun clearData() {
        _uiState.update {
            MediaOpinionUiState(isLoading = false, isLoadingGroupCritics = false)
        }
    }

    fun loadOpinions() {
        viewModelScope.launch {
            // Set loading state to true
            _uiState.update { it.copy(isLoading = true) }

            try {
                repository.getAllOpinions().collect { opinions ->
                    _uiState.update {
                        val currentQuery = it.searchQuery
                        val filtered = if (currentQuery.isNotEmpty()) {
                            filterOpinions(opinions, currentQuery)
                        } else {
                            opinions
                        }
                        it.copy(
                            opinions = opinions,
                            filteredOpinions = filtered,
                            isLoading = false
                        )
                    }
                }
                
                loadGroupCritics()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    /**
     * Carga las críticas de todos los grupos en los que el usuario es miembro
     */
    fun loadGroupCritics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingGroupCritics = true, error = null) }
                
                groupsRepository.getMemberGroupCritics(currentUserId).fold(
                    onSuccess = { criticsResponses ->
                        _uiState.update {
                            it.copy(
                                groupCritics = criticsResponses,
                                isLoadingGroupCritics = false
                            ) 
                        }
                        
                        // Guardar en caché
                        saveCriticsToCache(criticsResponses)
                    },
                    onFailure = { exception ->
                        // Intentar cargar desde la caché
                        loadCriticsFromCache()
                    }
                )
            } catch (e: Exception) {
                // Intentar cargar desde la caché
                loadCriticsFromCache()
            }
        }
    }
    
    /**
     * Guarda las críticas en la caché local
     */
    private fun saveCriticsToCache(critics: List<MediaOpinion>) {
        viewModelScope.launch {
            try {
                val cachedCritics = critics.map { critic ->
                    // Extraer la ruta parcial de las URLs completas
                    val posterPath = critic.posterUrl?.let { url ->
                        // Extraer la parte final de la URL (el path)
                        url.substringAfterLast("/", "")
                    }
                    
                    val backdropPath = critic.backdropUrl?.let { url ->
                        // Extraer la parte final de la URL (el path)
                        url.substringAfterLast("/", "")
                    }
                    
                    GroupCriticCached(
                        id = critic.id,
                        title = critic.title,
                        posterUrl = critic.posterUrl,
                        backdropUrl = critic.backdropUrl,
                        posterPath = posterPath,
                        backdropPath = backdropPath,
                        genre = critic.genre,
                        platform = critic.platform,
                        rating = critic.rating,
                        averageRating = critic.averageRating,
                        synopsis = critic.synopsis,
                        year = critic.year,
                        userId = currentUserId
                    )
                }
                
                groupCriticsCacheDao.updateUserCache(currentUserId, cachedCritics)
            } catch (e: Exception) {
                // Solo registramos el error, no afectamos la UI
                println("Error al guardar críticas en caché: ${e.message}")
            }
        }
    }
    
    /**
     * Carga las críticas desde la caché local si están disponibles
     */
    private suspend fun loadCriticsFromCache() {
        try {
            val hasCache = groupCriticsCacheDao.hasCacheForUser(currentUserId) > 0
            
            if (hasCache) {
                groupCriticsCacheDao.getCachedCriticsByUserId(currentUserId).collect { cachedCritics ->
                    if (cachedCritics.isNotEmpty()) {
                        val critics = cachedCritics.map { cached ->
                            MediaOpinion(
                                id = cached.id,
                                title = cached.title,
                                posterUrl = cached.posterUrl,
                                backdropUrl = cached.backdropUrl,
                                genre = cached.genre,
                                platform = cached.platform,
                                rating = cached.rating,
                                averageRating = cached.averageRating,
                                synopsis = cached.synopsis,
                                year = cached.year
                            )
                        }
                        
                        _uiState.update {
                            it.copy(
                                groupCritics = critics,
                                isLoadingGroupCritics = false,
                                // No establecemos error si tenemos datos en caché
                                error = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingGroupCritics = false,
                                error = "No hay recomendaciones de grupos disponibles"
                            )
                        }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingGroupCritics = false,
                        error = "No hay conexión a internet y no hay datos guardados"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoadingGroupCritics = false,
                    error = "Error al cargar críticas de grupos: ${e.message}"
                )
            }
        }
    }

    /**
     * Obtiene una crítica específica por su ID
     * @param id ID de la crítica a obtener
     * @param onResult Callback con el resultado (opinión o null si no se encuentra)
     */
    fun getOpinionById(id: Long, onResult: (MediaOpinion?) -> Unit) {
        viewModelScope.launch {
            try {
                val opinion = repository.getOpinionByIdDirect(id)
                onResult(opinion)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                onResult(null)
            }
        }
    }

    fun saveOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            try {
                repository.saveOpinion(opinion)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteOpinion(opinion: MediaOpinion) {
        viewModelScope.launch {
            try {
                repository.deleteOpinion(opinion)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Envía una calificación para una película o serie.
     * Si es una película de TMDB (no guardada), crea una nueva opinión.
     * Si ya existe, actualiza la calificación existente.
     *
     * @param opinion La opinión a actualizar o guardar
     * @param rating La nueva calificación (de 0 a 10)
     * @param onComplete Callback con el resultado (true = éxito, false = error)
     */
    fun submitRating(opinion: MediaOpinion, rating: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isRating = true) }

                val existingOpinion = repository.getOpinionByIdDirect(opinion.id)

                if (existingOpinion == null) {
                    val newOpinion = MediaOpinion(
                        id = opinion.id,
                        title = opinion.title,
                        platform = opinion.platform,
                        genre = opinion.genre,
                        rating = rating.toFloat(),
                        comments = emptyList(),
                        synopsis = opinion.synopsis,
                        posterUrl = opinion.posterUrl,
                        ratingCount = 1,
                        averageRating = rating.toFloat(),
                        year = opinion.year,
                        backdropUrl = opinion.backdropUrl
                    )
                    
                    val savedId = repository.saveOpinion(newOpinion)
                    if (savedId > 0) {
                        _uiState.update { it.copy(isRating = false) }
                        onComplete(true)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "No se pudo guardar la calificación",
                                isRating = false
                            )
                        }
                        onComplete(false)
                    }
                } else {
                    val newRatingCount = existingOpinion.ratingCount + 1
                    val totalRatingPoints = existingOpinion.averageRating * existingOpinion.ratingCount + rating
                    val newAverageRating = totalRatingPoints / newRatingCount

                    val updatedOpinion = existingOpinion.copy(
                        ratingCount = newRatingCount,
                        averageRating = newAverageRating
                    )

                    val success = repository.updateOpinionById(existingOpinion.id, updatedOpinion)

                    if (success) {
                        _uiState.update { currentState ->
                            val updatedOpinions = currentState.opinions.map {
                                if (it.id == existingOpinion.id) updatedOpinion else it
                            }
                            currentState.copy(opinions = updatedOpinions, isRating = false)
                        }

                        onComplete(true)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "No se pudo actualizar la calificación",
                                isRating = false
                            )
                        }
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error: ${e.message}",
                        isRating = false
                    )
                }
                onComplete(false)
            }
        }
    }

    /**
     * Actualiza la consulta de búsqueda y filtra las opiniones
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filteredOpinions = if (query.isNotEmpty()) {
                filterOpinions(currentState.opinions, query)
            } else {
                currentState.opinions
            }
            
            val filteredGroupCritics = if (query.isNotEmpty()) {
                filterOpinions(currentState.groupCritics, query)
            } else {
                currentState.groupCritics
            }
            
            currentState.copy(
                searchQuery = query,
                filteredOpinions = filteredOpinions,
                filteredGroupCritics = filteredGroupCritics
            )
        }
    }

    /**
     * Filtra las opiniones basándose en la consulta de búsqueda
     */
    private fun filterOpinions(opinions: List<MediaOpinion>, query: String): List<MediaOpinion> {
        if (query.isBlank()) return opinions

        val lowercaseQuery = query.lowercase()
        return opinions.filter { opinion ->
            opinion.title.lowercase().contains(lowercaseQuery) ||
                    opinion.genre.lowercase().contains(lowercaseQuery)
        }
    }

    /**
     * Limpia explícitamente el error en el estado
     */
    fun clearError() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
        }
    }
}

/**
 * UI state for media opinions
 */
data class MediaOpinionUiState(
    val opinions: List<MediaOpinion> = emptyList(),
    val filteredOpinions: List<MediaOpinion> = emptyList(),
    val groupCritics: List<MediaOpinion> = emptyList(),
    val filteredGroupCritics: List<MediaOpinion> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingGroupCritics: Boolean = false,
    val isRating: Boolean = false,
    val error: String? = null
)
