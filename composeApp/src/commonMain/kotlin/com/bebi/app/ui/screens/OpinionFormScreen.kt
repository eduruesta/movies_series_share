package com.bebi.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.app.model.MediaOpinion
import com.bebi.app.ui.components.StarRating
import com.bebi.app.viewmodel.MediaOpinionViewModel
import org.jetbrains.compose.resources.stringResource
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.comment_field
import moviesseriesshare.composeapp.generated.resources.content_rating_field
import moviesseriesshare.composeapp.generated.resources.duration_field
import moviesseriesshare.composeapp.generated.resources.genre_field
import moviesseriesshare.composeapp.generated.resources.image_field
import moviesseriesshare.composeapp.generated.resources.new_critic
import moviesseriesshare.composeapp.generated.resources.platform_field
import moviesseriesshare.composeapp.generated.resources.rating_field
import moviesseriesshare.composeapp.generated.resources.save_button
import moviesseriesshare.composeapp.generated.resources.search_online
import moviesseriesshare.composeapp.generated.resources.select_from_gallery
import moviesseriesshare.composeapp.generated.resources.title_field
import moviesseriesshare.composeapp.generated.resources.year_field
import org.koin.compose.koinInject

class OpinionFormScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var mediaOpinion by remember { mutableStateOf(MediaOpinion()) }
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinInject<MediaOpinionViewModel>()
        
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = mediaOpinion.title,
                    onValueChange = { mediaOpinion = mediaOpinion.copy(title = it) },
                    label = { Text(stringResource(Res.string.title_field)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Rating field with stars
                Column {
                    Text(
                        text = "${stringResource(Res.string.rating_field)}: ${mediaOpinion.rating.toInt()}/10",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    // Wrap the stars in a horizontally scrollable row for smaller screens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        StarRating(
                            rating = mediaOpinion.rating,
                            maxRating = 10,
                            onRatingChanged = { mediaOpinion = mediaOpinion.copy(rating = it) }
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
                        // If there's an image URL, we would display it here
                        // For now, just show a placeholder text
                        if (mediaOpinion.imageUrl.isEmpty()) {
                            Text(
                                text = "Vista previa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // In the future, we would load the image here
                            // For now, just show the URL
                            Text(
                                text = mediaOpinion.imageUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Image source buttons
                    Column(
                        modifier = Modifier
                            .width(150.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Will be implemented later */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.select_from_gallery))
                        }
                        
                        OutlinedButton(
                            onClick = { /* Will be implemented later */ },
                            modifier = Modifier.fillMaxWidth()
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
                        value = mediaOpinion.genre,
                        onValueChange = { mediaOpinion = mediaOpinion.copy(genre = it) },
                        label = { Text(stringResource(Res.string.genre_field)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    
                    // Platform field
                    OutlinedTextField(
                        value = mediaOpinion.platform,
                        onValueChange = { mediaOpinion = mediaOpinion.copy(platform = it) },
                        label = { Text(stringResource(Res.string.platform_field)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
                
                // Comment field
                OutlinedTextField(
                    value = mediaOpinion.comment,
                    onValueChange = { mediaOpinion = mediaOpinion.copy(comment = it) },
                    label = { Text(stringResource(Res.string.comment_field)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                
                // Save button
                Button(
                    onClick = { 
                        // Save the opinion using the ViewModel
                        viewModel.saveOpinion(mediaOpinion)
                        navigator.pop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(stringResource(Res.string.save_button))
                }
            }
        }
    }
}
