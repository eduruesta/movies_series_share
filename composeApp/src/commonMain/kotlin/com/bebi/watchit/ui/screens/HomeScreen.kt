package com.bebi.watchit.ui.screens

import androidx.compose.animation.core.animate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.bebi.watchit.data.repository.MediaOpinionRepository
import com.bebi.watchit.data.repository.TmdbRepository
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.ui.components.AppDrawerContent
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import com.bebi.watchit.viewmodel.TopMoviesViewModel
import com.bebi.watchit.viewmodel.TopSeriesViewModel
import com.bebi.watchit.viewmodel.TrendingMoviesViewModel
import com.bebi.watchit.viewmodel.TrendingSeriesViewModel
import com.bebi.watchit.viewmodel.UpcomingMoviesViewModel
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.app_name
import moviesseriesshare.composeapp.generated.resources.criticly_recommendations
import moviesseriesshare.composeapp.generated.resources.see_all
import moviesseriesshare.composeapp.generated.resources.top_movies
import moviesseriesshare.composeapp.generated.resources.top_series
import moviesseriesshare.composeapp.generated.resources.trending_movies
import moviesseriesshare.composeapp.generated.resources.trending_series
import moviesseriesshare.composeapp.generated.resources.upcoming_movies
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import placeholder

/**
 * Pantalla de inicio con diseño tipo streaming
 */
class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scope = rememberCoroutineScope()

        // ViewModels
        val mediaOpinionViewModel: MediaOpinionViewModel = koinViewModel()
        val trendingMoviesViewModel: TrendingMoviesViewModel = koinViewModel()
        val trendingSeriesViewModel: TrendingSeriesViewModel = koinViewModel()
        val topMoviesViewModel: TopMoviesViewModel = koinViewModel()
        val topSeriesViewModel: TopSeriesViewModel = koinViewModel()
        val upcomingMoviesViewModel: UpcomingMoviesViewModel = koinViewModel()

        // UI States
        val userOpinionsState by mediaOpinionViewModel.uiState.collectAsState()
        val trendingMoviesState by trendingMoviesViewModel.uiState.collectAsState()
        val trendingSeriesState by trendingSeriesViewModel.uiState.collectAsState()
        val topMoviesState by topMoviesViewModel.uiState.collectAsState()
        val topSeriesState by topSeriesViewModel.uiState.collectAsState()
        val upcomingMoviesState by upcomingMoviesViewModel.uiState.collectAsState()

        // Drawer state
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var drawerProgress by remember { mutableStateOf(0f) }

        // Efectos para la drawer animation
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

        LaunchedEffect(Unit) {
            mediaOpinionViewModel.loadOpinions()
        }

        val overlayAlpha = drawerProgress * 0.5f
        val contentOffset = drawerProgress * 200f


        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    onNavigateToMediaList = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(MediaListScreen())
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
                    onNavigateToGroups = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(GroupsScreen())
                        }
                    },
                    onNavigateToSettings = {
                        scope.launch {
                            drawerState.close()
                            navigator.push(SettingsScreen())
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
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = stringResource(Res.string.app_name),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
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
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    scrollBehavior = scrollBehavior
                                )
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        ) { paddingValues ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                if (userOpinionsState.groupCritics.isNotEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        MediaCarouselSection(
                                            title = "Recomendaciones de tus grupos",
                                            items = userOpinionsState.groupCritics,
                                            isLoading = userOpinionsState.isLoadingGroupCritics,
                                            onItemClick = { media ->
                                                navigator.push(MediaDetailScreen(media.id))
                                            },
                                            onSeeAllClick = {
                                                navigator.push(GroupsScreen())
                                            }
                                        )
                                    }
                                }

                                // Películas en tendencia
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediaCarouselSection(
                                        title = stringResource(Res.string.trending_movies),
                                        items = trendingMoviesState.mediaItems,
                                        isLoading = trendingMoviesState.isLoading,
                                        onItemClick = { media ->
                                            navigator.push(MediaDetailScreen(tmdbMediaOpinion = media))
                                        },
                                        onSeeAllClick = {
                                            navigator.push(TrendingMoviesScreen())
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediaCarouselSection(
                                        title = stringResource(Res.string.top_movies),
                                        items = topMoviesState.mediaItems,
                                        isLoading = topMoviesState.isLoading,
                                        onItemClick = { media ->
                                            navigator.push(MediaDetailScreen(tmdbMediaOpinion = media))
                                        },
                                        onSeeAllClick = {
                                            navigator.push(TopMoviesScreen())
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediaCarouselSection(
                                        title = stringResource(Res.string.upcoming_movies),
                                        items = upcomingMoviesState.mediaItems,
                                        isLoading = upcomingMoviesState.isLoading,
                                        onItemClick = { media ->
                                            navigator.push(MediaDetailScreen(tmdbMediaOpinion = media))
                                        },
                                        onSeeAllClick = {
                                            navigator.push(UpcomingMoviesScreen())
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediaCarouselSection(
                                        title = stringResource(Res.string.trending_series),
                                        items = trendingSeriesState.mediaItems,
                                        isLoading = trendingSeriesState.isLoading,
                                        onItemClick = { media ->
                                            navigator.push(MediaDetailScreen(tmdbMediaOpinion = media))
                                        },
                                        onSeeAllClick = {
                                            navigator.push(TrendingSeriesScreen())
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    MediaCarouselSection(
                                        title = stringResource(Res.string.top_series),
                                        items = topSeriesState.mediaItems,
                                        isLoading = topSeriesState.isLoading,
                                        onItemClick = { media ->
                                            navigator.push(MediaDetailScreen(tmdbMediaOpinion = media))
                                        },
                                        onSeeAllClick = {
                                            navigator.push(TopSeriesScreen())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}


/**
 * Sección de carrusel horizontal con título y botón "Ver todos"
 */
@Composable
fun MediaCarouselSection(
    title: String,
    items: List<MediaOpinion>,
    isLoading: Boolean,
    onItemClick: (MediaOpinion) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.clickable(onClick = onSeeAllClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.see_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay contenido disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { media ->
                    MediaPosterCard(
                        media = media,
                        onClick = { onItemClick(media) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar póster de película/serie en carrusel
 */
@Composable
fun MediaPosterCard(
    media: MediaOpinion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = media.posterUrl,
                contentDescription = media.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (media.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = media.rating.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
