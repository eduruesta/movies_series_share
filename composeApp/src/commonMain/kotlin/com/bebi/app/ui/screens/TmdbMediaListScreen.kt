package com.bebi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.app.data.remote.model.TmdbMediaItem
import com.bebi.app.data.repository.TmdbRepository
import com.bebi.app.ui.components.MediaOpinionItem
import com.bebi.app.viewmodel.TmdbMediaListViewModel
import com.bebi.app.viewmodel.TopMoviesViewModel
import com.bebi.app.viewmodel.TopSeriesViewModel
import com.bebi.app.viewmodel.TrendingMoviesViewModel
import com.bebi.app.viewmodel.TrendingSeriesViewModel
import com.bebi.app.viewmodel.UpcomingMoviesViewModel
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.top_movies
import moviesseriesshare.composeapp.generated.resources.top_series
import moviesseriesshare.composeapp.generated.resources.trending
import moviesseriesshare.composeapp.generated.resources.upcoming
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla base para mostrar listas de medios de TMDB
 * (películas o series) usando diferentes criterios (top, trending, etc.)
 */
abstract class TmdbMediaListScreen : Screen {
    @get:Composable
    abstract val title: String


    abstract suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>>

    @Composable
    protected abstract fun getViewModel(): TmdbMediaListViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scope = rememberCoroutineScope()

        val viewModel = getViewModel()

        val uiState by viewModel.uiState.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
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
                    scrollBehavior = scrollBehavior,
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

                    uiState.error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.Red
                            )
                        }
                    }

                    uiState.mediaItems.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No hay elementos para mostrar",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            itemsIndexed(uiState.mediaItems) { index, mediaOpinion ->
                                MediaOpinionItem(
                                    opinion = mediaOpinion,
                                    onClick = {
                                        navigator.push(
                                            MediaDetailScreen(
                                                tmdbMediaOpinion = mediaOpinion
                                            )
                                        )
                                    },
                                    onRateClick = {
                                        // No implementamos valoración para elementos de TMDB por ahora
                                    },
                                    onShowMessage = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message
                                            )
                                        }
                                    }
                                )
                                if (index < uiState.mediaItems.lastIndex) {
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

/**
 * Pantalla de Series mejor valoradas
 */
class TopSeriesScreen : TmdbMediaListScreen() {
    @Composable
    override fun getViewModel(): TmdbMediaListViewModel = koinViewModel<TopSeriesViewModel>()

    override val title: String
        @Composable
        get() = stringResource(Res.string.top_series)

    override suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedTvShows()
    }
}

/**
 * Pantalla de Series en tendencia
 */
class TrendingSeriesScreen : TmdbMediaListScreen() {
    @Composable
    override fun getViewModel(): TmdbMediaListViewModel = koinViewModel<TrendingSeriesViewModel>()


    override val title: String
        @Composable
        get() = stringResource(Res.string.trending)

    override suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>> {
        return repository.getTrendingTvShows()
    }
}

/**
 * Pantalla de Películas próximas a estrenarse
 */
class UpcomingMoviesScreen : TmdbMediaListScreen() {
    @Composable
    override fun getViewModel(): TmdbMediaListViewModel = koinViewModel<UpcomingMoviesViewModel>()

    override val title: String
        @Composable
        get() = stringResource(Res.string.upcoming)

    override suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>> {
        return repository.getUpcomingMovies()
    }
}

/**
 * Pantalla de Películas mejor valoradas
 */
class TopMoviesScreen : TmdbMediaListScreen() {
    @Composable
    override fun getViewModel(): TmdbMediaListViewModel = koinViewModel<TopMoviesViewModel>()


    override val title: String
        @Composable
        get() = stringResource(Res.string.top_movies)

    override suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>> {
        return repository.getTopRatedMovies()
    }
}

/**
 * Pantalla de Películas en tendencia
 */
class TrendingMoviesScreen : TmdbMediaListScreen() {
    @Composable
    override fun getViewModel(): TmdbMediaListViewModel = koinViewModel<TrendingMoviesViewModel>()


    override val title: String
        @Composable
        get() = stringResource(Res.string.trending)

    override suspend fun loadMedia(repository: TmdbRepository): Result<List<TmdbMediaItem>> {
        return repository.getTrendingMovies()
    }
}
