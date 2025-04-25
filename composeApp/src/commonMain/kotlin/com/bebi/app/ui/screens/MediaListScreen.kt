package com.bebi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.app.model.MediaOpinion
import com.bebi.app.model.SavedRecommendation
import com.bebi.app.ui.components.AppDrawerContent
import com.bebi.app.ui.components.RatingBottomSheet
import com.bebi.app.ui.components.bookmark
import com.bebi.app.ui.components.bookmarkCheck
import com.bebi.app.viewmodel.MediaOpinionViewModel
import com.bebi.app.viewmodel.RecommendationMessage
import com.bebi.app.viewmodel.SavedRecommendationViewModel
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.create_critic_button
import moviesseriesshare.composeapp.generated.resources.delete_from_recommendations
import moviesseriesshare.composeapp.generated.resources.empty_list_message
import moviesseriesshare.composeapp.generated.resources.image_field
import moviesseriesshare.composeapp.generated.resources.media_list_title
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
import kotlin.math.round

/**
 * Función de extensión para formatear un Float con un decimal fijo
 */
fun Float.formatWithOneDecimal(): String {
    val rounded = round(this * 10) / 10
    return if (rounded == rounded.toInt().toFloat()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/**
 * Pantalla principal para listar críticas de películas y series
 */
class MediaListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<MediaOpinionViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val uiState = viewModel.uiState.collectAsState().value
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var showRatingSheet by remember { mutableStateOf(false) }
        var selectedOpinion by remember { mutableStateOf<MediaOpinion?>(null) }

        // Estado para el drawer (menú lateral)
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        // Cargar las opiniones al entrar a la pantalla
        LaunchedEffect(Unit) {
            viewModel.loadOpinions()
        }

        // Implementamos el ModalNavigationDrawer como contenedor principal
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    onNavigateToMediaList = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onNavigateToProfile = {
                        scope.launch {
                            drawerState.close()
                            // Aquí puedes navegar a una pantalla de perfil cuando la tengas
                        }
                    },
                    onNavigateToRecommendations = {
                        scope.launch {
                            drawerState.close()
                            // Navegar a la pantalla de recomendaciones guardadas
                            navigator.push(SavedRecommendationScreen())
                        }
                    },
                    onNavigateToSettings = {
                        scope.launch {
                            drawerState.close()
                            // Aquí puedes navegar a configuración cuando la tengas
                        }
                    },
                    drawerState = drawerState
                )
            },
            gesturesEnabled = true
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(Res.string.media_list_title)) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navigator.push(OpinionFormScreen()) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(Res.string.create_critic_button)
                            )
                        }
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when {
                            uiState.isLoading -> {
                                // Show loading state
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            uiState.opinions.isEmpty() -> {
                                // Show empty state
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = stringResource(Res.string.empty_list_message),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            else -> {
                                // Show list of opinions
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(uiState.opinions) { index, opinion ->
                                        MediaOpinionItem(
                                            opinion = opinion,
                                            onClick = {
                                                // Navegar a la pantalla de detalle usando el ID de la opinión
                                                navigator.push(MediaDetailScreen(opinion.id))
                                            },
                                            onRateClick = {
                                                selectedOpinion = opinion
                                                showRatingSheet = true
                                            },
                                            onShowMessage = { message ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        )

                                        if (index < uiState.opinions.lastIndex) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showRatingSheet && selectedOpinion != null) {
                RatingBottomSheet(
                    opinion = selectedOpinion!!,
                    onDismiss = { if (!uiState.isRating) showRatingSheet = false },
                    isLoading = uiState.isRating,
                    onRatingSubmit = { opinion, rating ->
                        viewModel.submitRating(opinion, rating) { success ->
                            // Solo cerramos el BottomSheet si la calificación fue exitosa
                            if (success) {
                                showRatingSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MediaOpinionItem(
    opinion: MediaOpinion,
    onClick: () -> Unit,
    onRateClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val savedViewModel = koinViewModel<SavedRecommendationViewModel>()
    val coroutineScope = rememberCoroutineScope()

    // Estado para controlar si la opinión está guardada
    var isSaved by remember { mutableStateOf(false) }

    // Guardamos el último mensaje recibido para traducirlo en contexto @Composable
    var lastRecommendationMessage by remember { mutableStateOf<RecommendationMessage?>(null) }

    // Verificar si la opinión ya está guardada
    LaunchedEffect(opinion.id) {
        savedViewModel.isRecommendationSaved(opinion.id) { saved ->
            isSaved = saved
        }
    }

    // Traducir el mensaje cuando cambie (en contexto @Composable)
    lastRecommendationMessage?.let { message ->
        // Traducir el mensaje a un string usando stringResource
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
