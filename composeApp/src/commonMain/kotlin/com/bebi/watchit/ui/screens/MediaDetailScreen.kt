package com.bebi.watchit.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.bebi.watchit.ui.components.Add
import com.bebi.watchit.ui.components.Arrow_back
import com.bebi.watchit.ui.components.CastSection
import com.bebi.watchit.ui.components.CommentsSection
import com.bebi.watchit.ui.components.ErrorScreen
import com.bebi.watchit.ui.components.MediaInfoSection
import com.bebi.watchit.ui.components.Person
import com.bebi.watchit.ui.components.RecommendationsSection
import com.bebi.watchit.ui.components.StarRating
import com.bebi.watchit.ui.components.bookmark
import com.bebi.watchit.ui.components.bookmarkCheck
import com.bebi.watchit.ui.util.formatWithOneDecimal
import com.bebi.watchit.viewmodel.MediaDetailViewModel
import com.bebi.watchit.viewmodel.RecommendationMessage
import com.bebi.watchit.viewmodel.SavedRecommendationViewModel
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.add_new_comment
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel
import moviesseriesshare.composeapp.generated.resources.cast
import moviesseriesshare.composeapp.generated.resources.comment_label
import moviesseriesshare.composeapp.generated.resources.comments
import moviesseriesshare.composeapp.generated.resources.delete_from_recommendations
import moviesseriesshare.composeapp.generated.resources.loading_cast
import moviesseriesshare.composeapp.generated.resources.loading_cast_issue
import moviesseriesshare.composeapp.generated.resources.loading_details
import moviesseriesshare.composeapp.generated.resources.loading_recommendation
import moviesseriesshare.composeapp.generated.resources.loading_recommendation_issue
import moviesseriesshare.composeapp.generated.resources.loading_title
import moviesseriesshare.composeapp.generated.resources.media_list_title
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
import moviesseriesshare.composeapp.generated.resources.username_field
import moviesseriesshare.composeapp.generated.resources.username_placeholder
import moviesseriesshare.composeapp.generated.resources.username_required
import moviesseriesshare.composeapp.generated.resources.without_comment
import moviesseriesshare.composeapp.generated.resources.write_your_comment
import org.jetbrains.compose.resources.InternalResourceApi
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
        ExperimentalMaterial3Api::class, InternalResourceApi::class
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

        // Cargar los datos de la opinión al entrar a la pantalla
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
                savedViewModel.isRecommendationSaved(tmdbMediaOpinion.id) { saved ->
                    isSaved = saved
                }
            }
        }

        LaunchedEffect(uiState.opinion) {
            val opinion = uiState.opinion
            if (opinion != null && opinionId != null) {
                val isTvMedia = opinion.mediaType == "tv"
                val isMovieMedia = opinion.mediaType == "movie"
                if (isTvMedia || isMovieMedia) {
                    viewModel.loadCast(opinionId.toInt(), opinion.mediaType)
                }
                viewModel.loadRecommendationsMedia(opinionId.toInt(), opinion.mediaType)
            } else if (opinion != null && tmdbMediaOpinion != null) {
                val isTvMedia = tmdbMediaOpinion.mediaType == "tv"
                val isMovieMedia = tmdbMediaOpinion.mediaType == "movie"
                if (isTvMedia || isMovieMedia) {
                    viewModel.loadCast(tmdbMediaOpinion.id.toInt(), tmdbMediaOpinion.mediaType)
                }
                viewModel.loadRecommendationsMedia(
                    tmdbMediaOpinion.id.toInt(),
                    tmdbMediaOpinion.mediaType
                )
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
            var showUsernameError by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showCommentDialog = false },
                title = { Text(stringResource(Res.string.add_new_comment)) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Campo para el nombre del autor
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { 
                                viewModel.updateUsername(it)
                                showUsernameError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(Res.string.username_placeholder)) },
                            label = { Text(stringResource(Res.string.username_field)) },
                            singleLine = true,
                            isError = showUsernameError,
                            supportingText = if (showUsernameError) {
                                { Text(stringResource(Res.string.username_required)) }
                            } else null
                        )

                        // Campo para el comentario
                        OutlinedTextField(
                            value = uiState.newComment,
                            onValueChange = { viewModel.updateNewComment(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(Res.string.write_your_comment)) },
                            label = { Text(stringResource(Res.string.comment_label)) },
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Verificar que haya un nombre de usuario
                            if (uiState.username.isBlank()) {
                                // Mostrar error si no hay nombre de usuario
                                showUsernameError = true
                            } else {
                                viewModel.addComment(uiState.newComment, uiState.username)
                                showCommentDialog = false
                            }
                        },
                        enabled = uiState.newComment.isNotBlank()
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
                                imageVector = Arrow_back,
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
                        AdaptiveCircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(Res.string.loading_details))
                    }
                } else if (uiState.error != null) {
                    ErrorScreen(
                        onRetry = {
                            if (opinionId != null) {
                                viewModel.loadOpinionById(opinionId)
                            }
                        }
                    )
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

                        MediaInfoSection(
                            opinion = opinion,
                            isSaved = isSaved,
                            onBookmarkClick = {
                                if (isSaved) {
                                    val recommendation = SavedRecommendation(
                                        opinionId = opinion.id,
                                        title = opinion.title,
                                        posterUrl = opinion.posterUrl,
                                        rating = opinion.averageRating,
                                        genre = opinion.genre,
                                        backdropUrl = opinion.backdropUrl,
                                        overview = opinion.synopsis,
                                        platform = opinion.platform
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

                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {


                            // Sistema de pestañas (Tabs)
                            var selectedTab by remember { mutableStateOf(0) }
                            val tabs = buildList {
                                add(stringResource(Res.string.synopsis_field))
                                add(stringResource(Res.string.cast))
                                add(stringResource(Res.string.media_list_title))
                                if (opinion.groupId != null && !isTmbdMediaOpinion) {
                                    add(stringResource(Res.string.comments))
                                }
                            }

                            // Tabs
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ScrollableTabRow(
                                    selectedTabIndex = selectedTab,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    edgePadding = 24.dp,
                                    divider = {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                ) {
                                    tabs.forEachIndexed { index, title ->
                                        Tab(
                                            selected = selectedTab == index,
                                            onClick = {
                                                selectedTab = index
                                            },
                                            text = {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contenido de la tab seleccionada
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                when {
                                    // Sinopsis
                                    selectedTab == 0 && opinion.synopsis.isNotEmpty() -> {
                                        Column {
                                            Text(
                                                text = opinion.synopsis,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }

                                    // Cast
                                    selectedTab == 1 -> {
                                        CastSection(
                                            castList = uiState.cast,
                                            isLoadingCast = uiState.isLoadingCast
                                        )
                                    }

                                    // Similar
                                    selectedTab == 2 -> {
                                        RecommendationsSection(
                                            recommendationsMedia = uiState.recommendationsMedia,
                                            isLoadingRecommendationsMedia = uiState.isLoadingRecommendationsMedia
                                        )
                                    }

                                    // Comentarios (solo si hay groupId)
                                    !isTmbdMediaOpinion && selectedTab == 3 && opinion.groupId != null -> {
                                        CommentsSection(
                                            comments = opinion.comments,
                                            commentError = uiState.commentError?.toString(),
                                            onAddCommentClick = { showCommentDialog = true }
                                        )
                                    }

                                    // Si no hay información para mostrar
                                    else -> {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No hay información disponible",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(opinionId, uiState.opinion)
        {
            if (opinionId != null) {
                savedViewModel.isRecommendationSaved(opinionId) { saved ->
                    isSaved = saved
                }
            }
        }
    }
}
