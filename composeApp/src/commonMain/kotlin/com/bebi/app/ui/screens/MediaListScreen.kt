package com.bebi.app.ui.screens

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.app.model.MediaOpinion
import com.bebi.app.ui.components.AppDrawerContent
import com.bebi.app.ui.components.MediaOpinionItem
import com.bebi.app.ui.components.RatingBottomSheet
import com.bebi.app.ui.components.SearchTopAppBar
import com.bebi.app.viewmodel.MediaOpinionViewModel
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.create_critic_button
import moviesseriesshare.composeapp.generated.resources.empty_list_message
import moviesseriesshare.composeapp.generated.resources.media_list_title
import moviesseriesshare.composeapp.generated.resources.search_movies_series
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
        var showRatingSheet by remember { mutableStateOf(false) }
        var selectedOpinion by remember { mutableStateOf<MediaOpinion?>(null) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        var drawerProgress by remember { mutableStateOf(0f) }

        LaunchedEffect(drawerState) {
            snapshotFlow { drawerState.currentValue }
                .collect { value ->
                    drawerProgress = when (value) {
                        DrawerValue.Closed -> 0f
                        DrawerValue.Open -> 1f
                        else -> 0.5f
                    }
                }
        }

        LaunchedEffect(drawerState.targetValue, drawerState.currentValue) {
            if (drawerState.targetValue == DrawerValue.Open && drawerState.currentValue == DrawerValue.Closed) {
                animate(
                    initialValue = 0f,
                    targetValue = 1f
                ) { value, _ ->
                    drawerProgress = value
                }
            } else if (drawerState.targetValue == DrawerValue.Closed && drawerState.currentValue == DrawerValue.Open) {
                animate(
                    initialValue = 1f,
                    targetValue = 0f
                ) { value, _ ->
                    drawerProgress = value
                }
            }
        }

        val overlayAlpha = drawerProgress * 0.5f

        val contentOffset = drawerProgress * 200f

        LaunchedEffect(Unit) {
            viewModel.loadOpinions()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    onNavigateToMediaList = {
                        scope.launch {
                            drawerState.close()
                        }
                    },

                    onNavigateToRecommendations = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(SavedRecommendationScreen())
                        }
                    },
                    onNavigateToTopSeries = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(TopSeriesScreen())
                        }
                    },
                    onNavigateToTrendingSeries = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(TrendingSeriesScreen())
                        }
                    },
                    onNavigateToUpcomingMovies = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(UpcomingMoviesScreen())
                        }
                    },
                    onNavigateToTopMovies = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(TopMoviesScreen())
                        }
                    },
                    onNavigateToTrendingMovies = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(TrendingMoviesScreen())
                        }
                    },
                )
            },
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!drawerState.isClosed) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = overlayAlpha))
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                    }
                                }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = contentOffset
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Scaffold(
                                topBar = {
                                    SearchTopAppBar(
                                        title = stringResource(Res.string.media_list_title),
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
                                        onNavigationIconClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) {
                                                    drawerState.open()
                                                } else {
                                                    drawerState.close()
                                                }
                                            }
                                        },
                                        onSearchQueryChanged = { query ->
                                            viewModel.updateSearchQuery(query)
                                        },
                                        scrollBehavior = scrollBehavior,
                                        placeHolderText = stringResource(Res.string.search_movies_series)
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
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        uiState.opinions.isEmpty() -> {
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
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val displayedOpinions = if (uiState.searchQuery.isNotEmpty()) {
                                                    uiState.filteredOpinions
                                                } else {
                                                    uiState.opinions
                                                }
                                                
                                                itemsIndexed(displayedOpinions) { index, opinion ->
                                                    MediaOpinionItem(
                                                        opinion = opinion,
                                                        onClick = {
                                                            navigator.push(MediaDetailScreen(opinion.id))
                                                        },
                                                        onRateClick = {
                                                            selectedOpinion = opinion
                                                            showRatingSheet = true
                                                        },
                                                        onShowMessage = { message ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message
                                                                )
                                                            }
                                                        }
                                                    )

                                                    if (index < displayedOpinions.lastIndex) {
                                                        HorizontalDivider(
                                                            modifier = Modifier.padding(
                                                                vertical = 8.dp
                                                            )
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
                }
            }
        )
        if (showRatingSheet && selectedOpinion != null) {
            RatingBottomSheet(
                opinion = selectedOpinion!!,
                onDismiss = { if (!uiState.isRating) showRatingSheet = false },
                isLoading = uiState.isRating,
                onRatingSubmit = { opinion, rating ->
                    viewModel.submitRating(opinion, rating) { success ->
                        if (success) {
                            showRatingSheet = false
                        }
                    }
                }
            )
        }
    }
}
