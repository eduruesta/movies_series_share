package com.bebi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bebi.app.data.repository.MediaOpinionRepository
import com.bebi.app.model.MediaOpinion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing media opinions
 */
class MediaOpinionViewModel(
    private val repository: MediaOpinionRepository
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
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
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
     * Envía una calificación para una película o serie
     *
     * @param opinion La opinión a actualizar
     * @param rating La nueva calificación (de 0 a 10)
     * @return Devuelve true cuando la calificación se ha completado
     */
    fun submitRating(opinion: MediaOpinion, rating: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Indicar que estamos en proceso de calificación
                _uiState.update { it.copy(isRating = true) }

                // Calcular el nuevo promedio y contador de calificaciones
                val newRatingCount = opinion.ratingCount + 1

                // Si es la primera calificación, el promedio es igual a la calificación
                // Si no, calculamos el promedio ponderado
                val totalRatingPoints = opinion.averageRating * opinion.ratingCount + rating
                val newAverageRating = totalRatingPoints / newRatingCount

                // Aseguramos que se mantenga el ID original
                val updatedOpinion = opinion.copy(
                    // La calificación original se mantiene (es la del creador)
                    // pero actualizamos los campos de promedio y contador
                    ratingCount = newRatingCount,
                    averageRating = newAverageRating
                )

                val success = repository.updateOpinionById(opinion.id, updatedOpinion)

                if (success) {
                    // También podríamos actualizar el UI state directamente si queremos
                    // que la UI reaccione inmediatamente sin esperar a que el flow se actualice
                    _uiState.update { currentState ->
                        val updatedOpinions = currentState.opinions.map {
                            if (it.id == opinion.id) updatedOpinion else it
                        }
                        currentState.copy(opinions = updatedOpinions, isRating = false)
                    }

                    // Notificar que la calificación se ha completado con éxito
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
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isRating = false) }
                // Notificar que ha habido un error
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
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRating: Boolean = false,
    val error: String? = null
)
