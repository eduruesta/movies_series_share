package com.bebi.watchit.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.ui.components.AppDrawerContent
import com.bebi.watchit.ui.components.ErrorScreen
import com.bebi.watchit.ui.components.SkeletonPosterCard
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import com.bebi.watchit.viewmodel.TopMoviesViewModel
import com.bebi.watchit.viewmodel.TopSeriesViewModel
import com.bebi.watchit.viewmodel.TrendingMoviesViewModel
import com.bebi.watchit.viewmodel.TrendingSeriesViewModel
import com.bebi.watchit.viewmodel.UpcomingMoviesViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.app_name
import moviesseriesshare.composeapp.generated.resources.group_recommendations
import moviesseriesshare.composeapp.generated.resources.see_all
import moviesseriesshare.composeapp.generated.resources.top_movies
import moviesseriesshare.composeapp.generated.resources.top_series
import moviesseriesshare.composeapp.generated.resources.trending_movies
import moviesseriesshare.composeapp.generated.resources.trending_series
import moviesseriesshare.composeapp.generated.resources.upcoming_movies
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Pantalla de inicio con diseño tipo streaming
 */
class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val scope = rememberCoroutineScope()

        // ViewModels
        val user = Firebase.auth.currentUser
        val currentUserId = user?.uid ?: ""
        val mediaOpinionViewModel: MediaOpinionViewModel = koinViewModel { parametersOf(currentUserId) }
        val trendingMoviesViewModel: TrendingMoviesViewModel = koinViewModel()
        val trendingSeriesViewModel: TrendingSeriesViewModel = koinViewModel()
        val topMoviesViewModel: TopMoviesViewModel = koinViewModel()
        val topSeriesViewModel: TopSeriesViewModel = koinViewModel()
        val upcomingMoviesViewModel: UpcomingMoviesViewModel = koinViewModel()

        // UI States
        val mediaOpinionUiState by mediaOpinionViewModel.uiState.collectAsState()
        val trendingMoviesState by trendingMoviesViewModel.uiState.collectAsState()
        val trendingSeriesState by trendingSeriesViewModel.uiState.collectAsState()
        val topMoviesState by topMoviesViewModel.uiState.collectAsState()
        val topSeriesState by topSeriesViewModel.uiState.collectAsState()
        val upcomingMoviesState by upcomingMoviesViewModel.uiState.collectAsState()

        // Extensiones para limpiar errores explícitamente
        fun clearAllErrors() {
            scope.launch {
                // Forzamos la limpieza de errores en todos los ViewModels
                mediaOpinionViewModel.clearError()
                trendingMoviesViewModel.clearError() 
                trendingSeriesViewModel.clearError()
                topMoviesViewModel.clearError()
                topSeriesViewModel.clearError()
                upcomingMoviesViewModel.clearError()
            }
        }

        // Función para recargar todos los datos
        val reloadAllData: () -> Unit = {
            // Primero limpiamos todos los errores explícitamente
            clearAllErrors()
            
            // Luego recargamos los datos
            scope.launch {
                mediaOpinionViewModel.loadGroupCritics()
                trendingMoviesViewModel.loadMediaList()
                trendingSeriesViewModel.loadMediaList()
                topMoviesViewModel.loadMediaList()
                topSeriesViewModel.loadMediaList()
                upcomingMoviesViewModel.loadMediaList()
            }
        }

        // Cargar datos inicialmente
        LaunchedEffect(Unit) {
            reloadAllData()
        }

        // Drawer state
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                AppDrawerContent(
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
                    }
                )
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
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        scrollBehavior = scrollBehavior
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { paddingValues ->
                // Recalculamos hasError cada vez que cambia alguno de los estados
                // Esto garantiza que el valor se actualice reactivamente
                val hasError = mediaOpinionUiState.error != null &&
                    trendingMoviesState.error != null &&
                    trendingSeriesState.error != null &&
                    topMoviesState.error != null &&
                    topSeriesState.error != null &&
                    upcomingMoviesState.error != null

                if (hasError) {
                    ErrorScreen(
                        onRetry = reloadAllData,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    )
                } else {
                    // El resto del contenido normal
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Group recommendations section - mostrar solo si hay datos, no está vacío Y el usuario está autenticado
                        if (user != null && mediaOpinionUiState.groupCritics.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                MediaCarouselSection(
                                    title = stringResource(Res.string.group_recommendations),
                                    items = mediaOpinionUiState.groupCritics,
                                    isLoading = mediaOpinionUiState.isLoadingGroupCritics,
                                    onItemClick = { media ->
                                        navigator.push(MediaDetailScreen(media.id))
                                    },
                                    onSeeAllClick = {
                                        navigator.push(AllGroupRecommendationsScreen())
                                    }
                                )
                            }
                        }

                        // Trending Movies section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            MediaCarouselSection(
                                title = stringResource(Res.string.trending_movies),
                                items = trendingMoviesState.mediaItems,
                                isLoading = trendingMoviesState.isLoading,
                                onItemClick = { media ->
                                    navigator.push(
                                        MediaDetailScreen(
                                            tmdbMediaOpinion = media
                                        )
                                    )
                                },
                                onSeeAllClick = {
                                    navigator.push(TrendingMoviesScreen())
                                }
                            )
                        }

                        // Top Movies section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            MediaCarouselSection(
                                title = stringResource(Res.string.top_movies),
                                items = topMoviesState.mediaItems,
                                isLoading = topMoviesState.isLoading,
                                onItemClick = { media ->
                                    navigator.push(
                                        MediaDetailScreen(
                                            tmdbMediaOpinion = media
                                        )
                                    )
                                },
                                onSeeAllClick = {
                                    navigator.push(TopMoviesScreen())
                                }
                            )
                        }

                        // Upcoming Movies section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            MediaCarouselSection(
                                title = stringResource(Res.string.upcoming_movies),
                                items = upcomingMoviesState.mediaItems,
                                isLoading = upcomingMoviesState.isLoading,
                                onItemClick = { media ->
                                    navigator.push(
                                        MediaDetailScreen(
                                            tmdbMediaOpinion = media
                                        )
                                    )
                                },
                                onSeeAllClick = {
                                    navigator.push(UpcomingMoviesScreen())
                                }
                            )
                        }

                        // Trending Series section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            MediaCarouselSection(
                                title = stringResource(Res.string.trending_series),
                                items = trendingSeriesState.mediaItems,
                                isLoading = trendingSeriesState.isLoading,
                                onItemClick = { media ->
                                    navigator.push(
                                        MediaDetailScreen(
                                            tmdbMediaOpinion = media
                                        )
                                    )
                                },
                                onSeeAllClick = {
                                    navigator.push(TrendingSeriesScreen())
                                }
                            )
                        }

                        // Top Series section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            MediaCarouselSection(
                                title = stringResource(Res.string.top_series),
                                items = topSeriesState.mediaItems,
                                isLoading = topSeriesState.isLoading,
                                onItemClick = { media ->
                                    navigator.push(
                                        MediaDetailScreen(
                                            tmdbMediaOpinion = media
                                        )
                                    )
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Row(
                modifier = Modifier.clickable(onClick = onSeeAllClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.see_all),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                // Mostrar placeholders de carga
                items(10) {
                    SkeletonPosterCard()
                }
            } else if (items.isEmpty()) {
                // Mostrar mensaje cuando no hay elementos
                item {
                    Box(
                        modifier = Modifier
                            .width(240.dp)
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay contenido disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Mostrar elementos disponibles
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
        AsyncImage(
            model = media.posterUrl,
            contentDescription = media.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop,
        )
    }
}
