package com.bebi.watchit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.analytics.AnalyticsManager
import com.bebi.watchit.ui.components.SavedRecommendationItem
import com.bebi.watchit.ui.components.SearchTopAppBar
import com.bebi.watchit.viewmodel.SavedRecommendationViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.empty_recommendations_message
import moviesseriesshare.composeapp.generated.resources.recommendation_not_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_remove_error
import moviesseriesshare.composeapp.generated.resources.recommendation_removed
import moviesseriesshare.composeapp.generated.resources.search_recommendations
import moviesseriesshare.composeapp.generated.resources.your_recommendations
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla para mostrar las recomendaciones guardadas por el usuario
 */
class SavedRecommendationScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SavedRecommendationViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        
        LaunchedEffect(navigator) {
            viewModel.updateSearchQuery("")
        }

        var lastRecommendationMessage by remember { mutableStateOf<com.bebi.watchit.viewmodel.RecommendationMessage?>(null) }
        
        lastRecommendationMessage?.let { message ->
            val messageText = when (message) {
                is com.bebi.watchit.viewmodel.RecommendationMessage.Removed ->
                    stringResource(Res.string.recommendation_removed, message.title)
                is com.bebi.watchit.viewmodel.RecommendationMessage.ErrorRemoving ->
                    stringResource(Res.string.recommendation_remove_error, message.error)
                is com.bebi.watchit.viewmodel.RecommendationMessage.NotSaved ->
                    stringResource(Res.string.recommendation_not_saved)
                else -> null
            }
            
            messageText?.let {
                LaunchedEffect(messageText) {
                    snackbarHostState.showSnackbar(messageText)
                    lastRecommendationMessage = null
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.loadSavedRecommendations()
        }

        // Trackear vista de pantalla
        AnalyticsManager.trackScreenView(Firebase.analytics, "Saved Recommendations Screen")

        Scaffold(
            topBar = {
                SearchTopAppBar(
                    title = stringResource(Res.string.your_recommendations),
                    navigationIcon = {
                        IconButton(onClick = { 
                            navigator.pop() 
                            // Trackear clic en botón de navegación
                            AnalyticsManager.trackUiElementClick(
                                Firebase.analytics,
                                elementName = "back_button",
                                screenName = "Saved Recommendations Screen"
                            )
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás"
                            )
                        }
                    },
                    onNavigationIconClick = { navigator.pop() },
                    onSearchQueryChanged = { query ->
                        viewModel.updateSearchQuery(query)
                        // Trackear búsqueda si tiene suficientes caracteres
                        if (query.length > 2) {
                            AnalyticsManager.trackMediaSearch(Firebase.analytics, query, uiState.filteredRecommendations.size)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    placeHolderText = stringResource(Res.string.search_recommendations)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.savedRecommendations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.empty_recommendations_message),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val displayedRecommendations = if (uiState.searchQuery.isNotEmpty()) {
                                uiState.filteredRecommendations
                            } else {
                                uiState.savedRecommendations
                            }
                            
                            itemsIndexed(
                                items = displayedRecommendations,
                                key = { _, item -> item.opinionId } // Usar una clave estable para cada elemento
                            ) { index, savedRecommendation ->
                                SavedRecommendationItem(
                                    recommendation = savedRecommendation,
                                    onClick = {
                                        // Asegurarnos de que el id sea pasado como Long
                                        val opinionId: Long = savedRecommendation.opinionId
                                        navigator.push(MediaDetailScreen(opinionId = opinionId))
                                        // Trackear clic en recomendación guardada
                                        AnalyticsManager.trackUiElementClick(
                                            Firebase.analytics,
                                            elementName = "saved_recommendation_item",
                                            screenName = "Saved Recommendations Screen"
                                        )
                                        AnalyticsManager.trackMediaView(
                                            Firebase.analytics,
                                            mediaId = savedRecommendation.opinionId.toString(),
                                            mediaTitle = savedRecommendation.title,
                                            mediaType = "saved_recommendation"
                                        )
                                    },
                                    onRemoveClick = {
                                        // Trackear eliminación de recomendación
                                        AnalyticsManager.trackRemoveFromFavorites(
                                            Firebase.analytics,
                                            mediaId = savedRecommendation.opinionId.toString(),
                                            mediaTitle = savedRecommendation.title
                                        )
                                        viewModel.removeRecommendation(
                                            recommendation = savedRecommendation
                                        ) { message ->
                                            lastRecommendationMessage = message
                                        }
                                    }
                                )

                                if (index < displayedRecommendations.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }

                // Mostrar mensaje de error si existe
                uiState.error?.let { errorMessage ->
                    LaunchedEffect(errorMessage) {
                        snackbarHostState.showSnackbar(errorMessage)
                        // Trackear error
                        AnalyticsManager.trackError(
                            Firebase.analytics,
                            errorType = "saved_recommendations_error",
                            errorMessage = errorMessage,
                            screenName = "Saved Recommendations Screen"
                        )
                    }
                }
            }
        }
    }
}
