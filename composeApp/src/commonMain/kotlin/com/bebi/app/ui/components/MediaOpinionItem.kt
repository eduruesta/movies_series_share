package com.bebi.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation
import com.bebi.app.ui.screens.formatWithOneDecimal
import com.bebi.app.viewmodel.RecommendationMessage
import com.bebi.app.viewmodel.SavedRecommendationViewModel
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.delete_from_recommendations
import moviesseriesshare.composeapp.generated.resources.image_field
import moviesseriesshare.composeapp.generated.resources.opinion_count
import moviesseriesshare.composeapp.generated.resources.rate_action
import moviesseriesshare.composeapp.generated.resources.recommendation_already_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_not_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_remove_error
import moviesseriesshare.composeapp.generated.resources.recommendation_removed
import moviesseriesshare.composeapp.generated.resources.recommendation_save_error
import moviesseriesshare.composeapp.generated.resources.recommendation_saved
import moviesseriesshare.composeapp.generated.resources.save_as_recommendation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import placeholder

@Composable
fun MediaOpinionItem(
    opinion: MediaOpinion,
    onClick: () -> Unit,
    onRateClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedViewModel = koinViewModel<SavedRecommendationViewModel>()

    var isSaved by remember { mutableStateOf(false) }

    var lastRecommendationMessage by remember { mutableStateOf<RecommendationMessage?>(null) }

    LaunchedEffect(opinion.id) {
        savedViewModel.isRecommendationSaved(opinion.id) { saved ->
            isSaved = saved
        }
    }

    lastRecommendationMessage?.let { message ->
        val messageText = when (message) {
            is RecommendationMessage.Removed -> stringResource(
                Res.string.recommendation_removed,
                message.title
            )

            RecommendationMessage.NotSaved -> stringResource(Res.string.recommendation_not_saved)
            is RecommendationMessage.ErrorRemoving -> stringResource(
                Res.string.recommendation_remove_error,
                message.error
            )

            is RecommendationMessage.Saved -> stringResource(
                Res.string.recommendation_saved,
                message.title
            )

            is RecommendationMessage.AlreadySaved -> stringResource(
                Res.string.recommendation_already_saved,
                message.title
            )

            is RecommendationMessage.ErrorSaving -> stringResource(
                Res.string.recommendation_save_error,
                message.error
            )
        }

        // Mostrar el mensaje una sola vez
        LaunchedEffect(messageText) {
            onShowMessage(messageText)
            // Resetear el mensaje para que no se muestre de nuevo
            lastRecommendationMessage = null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (opinion.posterUrl.isNullOrEmpty()) {
                Image(
                    imageVector = placeholder,
                    contentDescription = opinion.title,
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = opinion.posterUrl,
                    contentDescription = stringResource(Res.string.image_field),
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = opinion.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Genre and Platform
                Text(
                    text = listOfNotNull(opinion.genre, opinion.platform)
                        .filter { it.isNotEmpty() }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rating row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Estrella de rating
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )

                    // Espacio después de la estrella
                    Spacer(modifier = Modifier.width(4.dp))

                    // Texto de calificación
                    val ratingText = if (opinion.ratingCount > 1) {
                        val formattedRating = opinion.averageRating.formatWithOneDecimal()
                        stringResource(
                            Res.string.opinion_count,
                            formattedRating,
                            opinion.ratingCount.toString()
                        )
                    } else {
                        "${opinion.rating}"
                    }

                    Text(
                        text = ratingText,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Espacio entre rating y botón calificar
                    Spacer(modifier = Modifier.width(16.dp))

                    // Botón Calificar
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onRateClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icono calificar
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(Res.string.rate_action),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Texto calificar
                        Text(
                            text = stringResource(Res.string.rate_action),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Espacio entre calificar y guardar
                    Spacer(modifier = Modifier.width(16.dp))

                    // Icono de guardado
                    Icon(
                        // Mostrar bookmarkCheck si ya está guardado, o bookmark si no
                        imageVector = if (isSaved) bookmarkCheck else bookmark,
                        contentDescription = if (isSaved)
                            stringResource(Res.string.delete_from_recommendations) else
                            stringResource(Res.string.save_as_recommendation),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                if (isSaved) {
                                    // Si ya está guardado, lo eliminamos
                                    // Crear un objeto SavedRecommendation con la información necesaria
                                    val recommendation = SavedRecommendation(
                                        opinionId = opinion.id,
                                        title = opinion.title,
                                        posterUrl = opinion.posterUrl,
                                        rating = opinion.averageRating,
                                        genre = opinion.genre,
                                        backdropUrl = opinion.backdropUrl
                                    )
                                    savedViewModel.removeRecommendation(
                                        recommendation = recommendation
                                    ) { message ->
                                        if (message is RecommendationMessage.Removed) {
                                            // Actualizar estado local
                                            isSaved = false
                                        }
                                        // Almacenar el mensaje para procesarlo en un contexto @Composable
                                        lastRecommendationMessage = message
                                    }
                                } else {
                                    // Si no está guardado, lo guardamos
                                    savedViewModel.saveRecommendation(
                                        opinion = opinion
                                    ) { message ->
                                        if (message is RecommendationMessage.Saved ||
                                            message is RecommendationMessage.AlreadySaved
                                        ) {
                                            // Actualizar estado local
                                            isSaved = true
                                        }
                                        // Almacenar el mensaje para procesarlo en un contexto @Composable
                                        lastRecommendationMessage = message
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}