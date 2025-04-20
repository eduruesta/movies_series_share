package com.bebi.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.app.ui.components.StarRating
import com.bebi.app.viewmodel.MediaOpinionFormViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.comment_field
import moviesseriesshare.composeapp.generated.resources.genre_field
import moviesseriesshare.composeapp.generated.resources.image_field
import moviesseriesshare.composeapp.generated.resources.new_critic
import moviesseriesshare.composeapp.generated.resources.platform_field
import moviesseriesshare.composeapp.generated.resources.rating_field
import moviesseriesshare.composeapp.generated.resources.save_button
import moviesseriesshare.composeapp.generated.resources.search_online
import moviesseriesshare.composeapp.generated.resources.select_from_gallery
import moviesseriesshare.composeapp.generated.resources.synopsis_field
import moviesseriesshare.composeapp.generated.resources.title_field
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen for creating or editing a media opinion
 */
@Serializable
class OpinionFormScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MediaOpinionFormViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // Show success message and navigate back when saved
        LaunchedEffect(uiState.saved) {
            if (uiState.saved) {
                navigator.pop()
            }
        }

        // Show search message if available
        LaunchedEffect(uiState.searchMessage) {
            uiState.searchMessage?.let {
                scope.launch {
                    snackbarHostState.showSnackbar(it)
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.new_critic)) },
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
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title field with search button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text(stringResource(Res.string.title_field)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { viewModel.searchMedia(viewModel.title) }
                        )
                    )

                    // Search button
                    IconButton(
                        onClick = { viewModel.searchMedia(viewModel.title) },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    ) {
                        if (viewModel.isSearching) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.width(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Error message for search
                viewModel.searchError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Rating field with stars
                Column {
                    Text(
                        text = "${stringResource(Res.string.rating_field)}: ${viewModel.rating}/10",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Wrap the stars in a horizontally scrollable row for smaller screens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        StarRating(
                            rating = viewModel.rating,
                            maxRating = 10,
                            onRatingChanged = { viewModel.updateRating(it.toFloat()) }
                        )
                    }
                }

                // Image field
                Text(
                    text = stringResource(Res.string.image_field),
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image preview area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = viewModel.updatePosterUrl(viewModel.posterUrl),
                            contentDescription = "Póster",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Image source buttons
                    Column(
                        modifier = Modifier
                            .width(150.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Will be implemented in the future */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.select_from_gallery))
                        }

                        OutlinedButton(
                            onClick = { viewModel.searchMedia(viewModel.title) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = viewModel.title.isNotBlank() && !viewModel.isSearching
                        ) {
                            Text(stringResource(Res.string.search_online))
                        }
                    }
                }

                // Genre and Platform fields in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Genre field
                    OutlinedTextField(
                        value = viewModel.genre,
                        onValueChange = { viewModel.updateGenre(it) },
                        label = { Text(stringResource(Res.string.genre_field)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    // Platform field
                    OutlinedTextField(
                        value = viewModel.platform,
                        onValueChange = { viewModel.updatePlatform(it) },
                        label = { Text(stringResource(Res.string.platform_field)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }

                // Synopsis field
                OutlinedTextField(
                    value = viewModel.synopsis,
                    onValueChange = { viewModel.updateSynopsis(it) },
                    label = { Text(stringResource(Res.string.synopsis_field)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Comment field
                OutlinedTextField(
                    value = viewModel.comment,
                    onValueChange = { viewModel.updateComment(it) },
                    label = { Text(stringResource(Res.string.comment_field)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = { viewModel.saveOpinion() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.title.isNotBlank()
                ) {
                    Text(stringResource(Res.string.save_button))
                }
            }
        }
    }
}
