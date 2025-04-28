package com.bebi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.app.ui.components.StarRating
import com.bebi.app.viewmodel.MediaDetailError
import com.bebi.app.viewmodel.MediaDetailViewModel
import kotlinx.serialization.Serializable
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.back
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.comment
import moviesseriesshare.composeapp.generated.resources.error_loading_details
import moviesseriesshare.composeapp.generated.resources.error_opinion_not_found
import moviesseriesshare.composeapp.generated.resources.loading_details
import moviesseriesshare.composeapp.generated.resources.loading_title
import moviesseriesshare.composeapp.generated.resources.opinion_count
import moviesseriesshare.composeapp.generated.resources.synopsis_field
import moviesseriesshare.composeapp.generated.resources.without_comment
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import placeholder

/**
 * Extrae solo el año de una fecha con formato YYYY-MM-DD
 */
fun extractYearFromDate(dateString: String): String {
    return if (dateString.contains("-") && dateString.length >= 4) {
        dateString.split("-")[0]
    } else {
        dateString
    }
}

/**
 * Screen that displays the details of a media opinion
 * Now uses only the ID for serialization safety
 */
@Serializable
class MediaDetailScreen(private val opinionId: Long) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MediaDetailViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        LaunchedEffect(opinionId) {
            viewModel.loadOpinionById(opinionId)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(uiState.opinion?.title ?: stringResource(Res.string.loading_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back_button)
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(Res.string.loading_details))
                    }
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val errorMessage = when (uiState.error) {
                            MediaDetailError.NOT_FOUND -> stringResource(Res.string.error_opinion_not_found)
                            MediaDetailError.GENERIC -> stringResource(Res.string.error_loading_details)
                            else -> stringResource(Res.string.error_loading_details)
                        }
                        
                        Text(
                            errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navigator.pop() }) {
                            Text(stringResource(Res.string.back))
                        }
                    }
                } else if (uiState.opinion != null) {
                    val opinion = uiState.opinion!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        AsyncImage(
                            model = opinion.backdropUrl ?: opinion.posterUrl ?: placeholder,
                            contentDescription = opinion.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                            Text(
                                text = opinion.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarRating(
                                    rating = opinion.rating,
                                    maxRating = 1,
                                )
                                val ratingText = if (opinion.ratingCount > 0) {
                                    val formattedRating =
                                        opinion.averageRating.formatWithOneDecimal()
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

                                if (opinion.year.isNotEmpty()) {
                                    FilledTonalButton(
                                        onClick = { },
                                        modifier = Modifier.height(32.dp).padding(start = 8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            extractYearFromDate(opinion.year),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }

                            if (opinion.synopsis.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.synopsis_field),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = opinion.synopsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (opinion.genre.isNotEmpty() || opinion.platform.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (opinion.genre.isNotEmpty()) {
                                        FilledTonalButton(
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
                                        FilledTonalButton(
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

                            Text(
                                text = stringResource(Res.string.comment),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = opinion.comment.ifEmpty { stringResource(Res.string.without_comment) },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
