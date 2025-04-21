package com.bebi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.app.ui.components.StarRating
import com.bebi.app.viewmodel.MediaDetailViewModel
import kotlinx.serialization.Serializable
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.opinion_count
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import placeholder

/**
 * Screen that displays the details of a media opinion
 * Now uses only the ID for serialization safety
 */
@Serializable
class MediaDetailScreen(private val opinionId: Long) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalResourceApi::class,
        KoinExperimentalAPI::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MediaDetailViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        // Load the opinion when the screen is first composed
        LaunchedEffect(opinionId) {
            viewModel.loadOpinionById(opinionId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(uiState.opinion?.title ?: "Cargando...") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)

        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    // Loading state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando detalles...")
                    }
                } else if (uiState.error != null) {
                    // Error state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navigator.pop() }) {
                            Text("Volver")
                        }
                    }
                } else if (uiState.opinion != null) {
                    // Content state - opinion details
                    val opinion = uiState.opinion!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Image
                        if (!opinion.posterUrl.isNullOrEmpty()) {
                            // If there's an image URL, we would load it here
                            // For now, just show a placeholder
                            AsyncImage(
                                model = opinion.posterUrl,
                                contentDescription = opinion.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                imageVector = placeholder,
                                contentDescription = opinion.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = opinion.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating display
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(
                                rating = opinion.rating,
                                maxRating = 1,
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            val ratingText = if (opinion.ratingCount > 0) {
                                // Usando una función de extensión compatible con KMP
                                val formattedRating = opinion.averageRating.formatWithOneDecimal()
                                stringResource(
                                    Res.string.opinion_count,
                                    formattedRating,
                                    opinion.ratingCount.toString()
                                )
                            } else {
                                // Si no hay calificaciones, mostramos la calificación original
                                "${opinion.rating}/10"
                            }

                            Text(
                                text = ratingText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Sinopsis
                        if (opinion.synopsis.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Sinopsis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = opinion.synopsis ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Additional metadata
                        if (opinion.year.isNotEmpty() || opinion.duration.isNotEmpty() || opinion.contentRating.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (opinion.year.isNotEmpty()) {
                                    FilledTonalButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            opinion.year,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (opinion.duration.isNotEmpty()) {
                                    FilledTonalButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            opinion.duration,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (opinion.contentRating.isNotEmpty()) {
                                    FilledTonalButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            opinion.contentRating,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Genre and Platform
                        if (opinion.genre.isNotEmpty() || opinion.platform.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (opinion.genre.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            opinion.genre,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (opinion.platform.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            opinion.platform,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Comment section
                        Text(
                            text = "Comentario:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = opinion.comment.ifEmpty { "Sin comentarios" },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
