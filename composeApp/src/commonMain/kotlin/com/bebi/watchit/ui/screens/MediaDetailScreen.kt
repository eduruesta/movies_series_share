package com.bebi.watchit.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.model.SavedRecommendation
import com.bebi.watchit.ui.components.StarRating
import com.bebi.watchit.ui.components.bookmark
import com.bebi.watchit.ui.components.bookmarkCheck
import com.bebi.watchit.ui.util.formatWithOneDecimal
import com.bebi.watchit.viewmodel.MediaDetailError
import com.bebi.watchit.viewmodel.MediaDetailViewModel
import com.bebi.watchit.viewmodel.RecommendationMessage
import com.bebi.watchit.viewmodel.SavedRecommendationViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.add_new_comment
import moviesseriesshare.composeapp.generated.resources.back
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel
import moviesseriesshare.composeapp.generated.resources.comment
import moviesseriesshare.composeapp.generated.resources.delete_from_recommendations
import moviesseriesshare.composeapp.generated.resources.error_loading_details
import moviesseriesshare.composeapp.generated.resources.error_opinion_not_found
import moviesseriesshare.composeapp.generated.resources.loading_details
import moviesseriesshare.composeapp.generated.resources.loading_title
import moviesseriesshare.composeapp.generated.resources.opinion_count
import moviesseriesshare.composeapp.generated.resources.recommendation_already_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_not_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_remove_error
import moviesseriesshare.composeapp.generated.resources.recommendation_removed
import moviesseriesshare.composeapp.generated.resources.recommendation_save_error
import moviesseriesshare.composeapp.generated.resources.recommendation_saved
import moviesseriesshare.composeapp.generated.resources.save
import moviesseriesshare.composeapp.generated.resources.save_to_recommendations
import moviesseriesshare.composeapp.generated.resources.synopsis_field
import moviesseriesshare.composeapp.generated.resources.without_comment
import moviesseriesshare.composeapp.generated.resources.write_your_comment
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
data class MediaDetailScreen(
    private val opinionId: Long? = null,
    private val tmdbMediaOpinion: MediaOpinion? = null
) : Screen {

    constructor(opinionId: Long) : this(opinionId = opinionId, tmdbMediaOpinion = null)

    constructor(tmdbMediaOpinion: MediaOpinion) : this(
        opinionId = null,
        tmdbMediaOpinion = tmdbMediaOpinion
    )

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MediaDetailViewModel = koinViewModel()
        val savedViewModel: SavedRecommendationViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        var isTmbdMediaOpinion by remember { mutableStateOf(false) }
        var showCommentDialog by remember { mutableStateOf(false) }

        var isSaved by remember { mutableStateOf(false) }

        var lastRecommendationMessage by remember { mutableStateOf<RecommendationMessage?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(opinionId, tmdbMediaOpinion) {
            if (opinionId != null) {
                viewModel.loadOpinionById(opinionId)
                isTmbdMediaOpinion = false
                savedViewModel.isRecommendationSaved(opinionId) { saved ->
                    isSaved = saved
                }
            } else if (tmdbMediaOpinion != null) {
                viewModel.setTmdbMediaOpinion(tmdbMediaOpinion)
                isTmbdMediaOpinion = true
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

            LaunchedEffect(messageText) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(messageText)
                }
                lastRecommendationMessage = null
            }
        }

        if (showCommentDialog) {
            AlertDialog(
                onDismissRequest = { showCommentDialog = false },
                title = { Text(stringResource(Res.string.add_new_comment)) },
                text = {
                    OutlinedTextField(
                        value = uiState.newComment,
                        onValueChange = { viewModel.updateNewComment(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(Res.string.write_your_comment)) },
                        minLines = 3
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.addComment(uiState.newComment)
                            showCommentDialog = false
                        }
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showCommentDialog = false
                        }
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (uiState.isLoading) stringResource(Res.string.loading_title) else uiState.opinion?.title
                                ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(Res.string.back_button)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        if (opinion.backdropUrl == null && opinion.posterUrl == null) {
                            Image(
                                imageVector = placeholder,
                                contentDescription = opinion.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )

                        } else {
                            AsyncImage(
                                model = opinion.backdropUrl ?: opinion.posterUrl,
                                contentDescription = opinion.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }


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
                                    rating = opinion.averageRating,
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
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
                                        
                                        // Mostrar el nombre del usuario si está disponible
                                        if (!opinion.username.isNullOrEmpty()) {
                                            FilledTonalButton(
                                                onClick = { },
                                                modifier = Modifier.height(32.dp).padding(start = 8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = opinion.username,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        // Icono de bookmark
                                        Icon(
                                            imageVector = if (isSaved) bookmarkCheck else bookmark,
                                            contentDescription = if (isSaved)
                                                stringResource(Res.string.delete_from_recommendations) else
                                                stringResource(Res.string.save_to_recommendations),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(start = 8.dp)
                                                .clickable {
                                                    if (isSaved) {
                                                        val recommendation = SavedRecommendation(
                                                            opinionId = opinion.id,
                                                            title = opinion.title,
                                                            posterUrl = opinion.posterUrl,
                                                            rating = opinion.averageRating,
                                                            genre = opinion.genre,
                                                            backdropUrl = opinion.backdropUrl,
                                                            overview = opinion.synopsis
                                                        )
                                                        savedViewModel.removeRecommendation(
                                                            recommendation = recommendation
                                                        ) { message ->
                                                            if (message is RecommendationMessage.Removed) {
                                                                isSaved = false
                                                            }
                                                            lastRecommendationMessage = message
                                                        }
                                                    } else {
                                                        savedViewModel.saveRecommendation(
                                                            opinion = opinion
                                                        ) { message ->
                                                            if (message is RecommendationMessage.Saved) {
                                                                isSaved = true
                                                            }
                                                            lastRecommendationMessage = message
                                                        }
                                                    }
                                                }
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
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (opinion.genre.isNotEmpty()) {
                                        FilledTonalButton(
                                            onClick = { },
                                            modifier = Modifier.wrapContentWidth(),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                opinion.genre,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (opinion.platform.isNotEmpty()) {
                                        FilledTonalButton(
                                            onClick = { },
                                            modifier = Modifier.wrapContentWidth(),
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

                            Spacer(modifier = Modifier.height(16.dp))

                            val shouldShowComments = opinion.groupId != null

                            if (shouldShowComments) {
                                Text(
                                    text = stringResource(Res.string.comment),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (opinion.comments.isEmpty()) {
                                    Text(
                                        text = stringResource(Res.string.without_comment),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        opinion.comments.forEachIndexed { index, comment ->
                                            val isEven = index % 2 == 0
                                            val backgroundColor = if (isEven)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.secondaryContainer

                                            val contentColor = if (isEven)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSecondaryContainer

                                            val alignment = if (isEven)
                                                Arrangement.Start
                                            else
                                                Arrangement.End

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = alignment
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .widthIn(max = 280.dp)
                                                        .padding(vertical = 4.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = backgroundColor,
                                                        contentColor = contentColor
                                                    ),
                                                    shape = RoundedCornerShape(
                                                        topStart = if (!isEven) 12.dp else 4.dp,
                                                        topEnd = if (isEven) 12.dp else 4.dp,
                                                        bottomStart = 12.dp,
                                                        bottomEnd = 12.dp
                                                    )
                                                ) {
                                                    Text(
                                                        text = comment,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (uiState.commentError != null) {
                                    Text(
                                        text = uiState.commentError.toString(),
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { showCommentDialog = true },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(stringResource(Res.string.add_new_comment))
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(opinionId, uiState.opinion) {
            if (opinionId != null) {
                savedViewModel.isRecommendationSaved(opinionId) { saved ->
                    isSaved = saved
                }
            }
        }
    }
}
