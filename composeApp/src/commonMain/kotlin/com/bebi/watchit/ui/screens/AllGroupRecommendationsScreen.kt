package com.bebi.watchit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.ui.components.ErrorScreen
import com.bebi.watchit.ui.components.MediaOpinionItem
import com.bebi.watchit.ui.components.RatingBottomSheet
import com.bebi.watchit.ui.components.SearchTopAppBar
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.group_recommendations
import moviesseriesshare.composeapp.generated.resources.search
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Pantalla para mostrar todas las recomendaciones de los grupos
 */
class AllGroupRecommendationsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val scope = rememberCoroutineScope()
        
        // Obtener el usuario autenticado
        val auth = remember { Firebase.auth }
        val firebaseUser: FirebaseUser? by remember { mutableStateOf(auth.currentUser) }
        val userId = firebaseUser?.uid ?: "anonymous_user"
        
        // ViewModel
        val mediaOpinionViewModel = koinInject<MediaOpinionViewModel> { parametersOf(userId) }
        val uiState by mediaOpinionViewModel.uiState.collectAsState()
        
        var showRatingSheet by remember { mutableStateOf(false) }
        var selectedOpinion by remember { mutableStateOf<MediaOpinion?>(null) }
        
        // Cargar las recomendaciones de grupos
        LaunchedEffect(Unit) {
            mediaOpinionViewModel.loadGroupCritics()
        }
        
        Scaffold(
            topBar = {
                SearchTopAppBar(
                    title = stringResource(Res.string.group_recommendations),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back_button)
                            )
                        }
                    },
                    onNavigationIconClick = { navigator.pop() },
                    onSearchQueryChanged = { query ->
                        mediaOpinionViewModel.updateSearchQuery(query)
                    },
                    scrollBehavior = scrollBehavior,
                    placeHolderText = stringResource(Res.string.search)
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
                    uiState.isLoadingGroupCritics -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    uiState.error != null -> {
                        ErrorScreen(
                            onRetry = {
                                mediaOpinionViewModel.loadGroupCritics()
                            }
                        )
                    }
                    
                    uiState.groupCritics.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No tienes recomendaciones de grupos todavía",
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
                            val displayedItems = if (uiState.searchQuery.isNotEmpty()) {
                                uiState.filteredGroupCritics
                            } else {
                                uiState.groupCritics
                            }
                            
                            itemsIndexed(
                                items = displayedItems,
                                key = { _, item -> item.id } // Usar key estable para animaciones
                            ) { index, media ->
                                MediaOpinionItem(
                                    opinion = media,
                                    onClick = {
                                        navigator.push(MediaDetailScreen(media.id))
                                    },
                                    onRateClick = {
                                        selectedOpinion = media
                                        showRatingSheet = true
                                    },
                                    onShowMessage = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                )
                                
                                if (index < displayedItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
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
                onDismiss = { if (!uiState.isLoading) showRatingSheet = false },
                isLoading = uiState.isLoading,
                onRatingSubmit = { opinion, rating ->
                    mediaOpinionViewModel.submitRating(opinion, rating) { success ->
                        if (success) {
                            showRatingSheet = false
                        }
                    }
                }
            )
        }
    }
}
