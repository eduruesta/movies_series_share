package com.bebi.watchit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.watchit.data.repository.GroupsRepository
import com.bebi.watchit.data.repository.MediaOpinionRepository
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
    private val currentUserId: String = "user_default_id" // Este valor debería venir de una sesión real
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaOpinionUiState())
    val uiState: StateFlow<MediaOpinionUiState> = _uiState.asStateFlow()

    // Se quita el init para cargar datos bajo demanda

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
                
                // También cargamos las críticas de grupos donde el usuario es miembro
                loadGroupCritics()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
    
    /**
     * Carga las críticas de todos los grupos en los que el usuario es miembro
     */
    private fun loadGroupCritics() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingGroupCritics = true) }
                
                groupsRepository.getMemberGroupCritics(currentUserId).fold(
                    onSuccess = { criticsResponses ->
                        _uiState.update {
                            it.copy(
                                groupCritics = criticsResponses,
                                isLoadingGroupCritics = false
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar críticas de grupos: ${exception.message}", 
                                isLoadingGroupCritics = false
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar críticas de grupos: ${e.message}",
                        isLoadingGroupCritics = false
                    ) 
                }
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
                    val randomId = kotlin.random.Random.nextLong(1_000_000, Long.MAX_VALUE)
                    
                    val newOpinion = MediaOpinion(
                        id = randomId,
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
            val filtered = if (query.isNotEmpty()) {
                filterOpinions(currentState.opinions, query)
            } else {
                currentState.opinions
            }
            currentState.copy(
                searchQuery = query,
                filteredOpinions = filtered
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
}

/**
 * UI state for media opinions
 */
data class MediaOpinionUiState(
    val opinions: List<MediaOpinion> = emptyList(),
    val filteredOpinions: List<MediaOpinion> = emptyList(),
    val groupCritics: List<MediaOpinion> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingGroupCritics: Boolean = false,
    val isRating: Boolean = false,
    val error: String? = null
)
