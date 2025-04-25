package com.bebi.app.ui.screens

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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.app.ui.components.SavedRecommendationItem
import com.bebi.app.viewmodel.SavedRecommendationViewModel
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.empty_recommendations_message
import moviesseriesshare.composeapp.generated.resources.recommendation_not_saved
import moviesseriesshare.composeapp.generated.resources.recommendation_remove_error
import moviesseriesshare.composeapp.generated.resources.recommendation_removed
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
        val viewModel = koinViewModel<SavedRecommendationViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val uiState by viewModel.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        // Variable para almacenar el último mensaje de recomendación
        var lastRecommendationMessage by remember { mutableStateOf<com.bebi.app.viewmodel.RecommendationMessage?>(null) }
        
        // Formatear los mensajes de recomendación cuando existan
        lastRecommendationMessage?.let { message ->
            val messageText = when (message) {
                is com.bebi.app.viewmodel.RecommendationMessage.Removed ->
                    stringResource(Res.string.recommendation_removed, message.title)
                is com.bebi.app.viewmodel.RecommendationMessage.ErrorRemoving ->
                    stringResource(Res.string.recommendation_remove_error, message.error)
                is com.bebi.app.viewmodel.RecommendationMessage.NotSaved ->
                    stringResource(Res.string.recommendation_not_saved)
                else -> null
            }
            
            // Mostrar el mensaje una sola vez
            messageText?.let {
                LaunchedEffect(messageText) {
                    snackbarHostState.showSnackbar(messageText)
                    // Resetear el mensaje para no mostrarlo nuevamente
                    lastRecommendationMessage = null
                }
            }
        }

        // Cargar las recomendaciones guardadas al entrar a la pantalla
        LaunchedEffect(Unit) {
            viewModel.loadSavedRecommendations()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.your_recommendations)) },
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
                    ),
                    scrollBehavior = scrollBehavior
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
                        // Mostrar estado de carga
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.savedRecommendations.isEmpty() -> {
                        // Mostrar estado vacío
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
                        // Mostrar lista de recomendaciones guardadas
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.savedRecommendations) { index, savedRecommendation ->
                                SavedRecommendationItem(
                                    recommendation = savedRecommendation,
                                    onClick = {
                                        // Navegar a la pantalla de detalle si es necesario
                                        navigator.push(MediaDetailScreen(savedRecommendation.opinionId))
                                    },
                                    onRemoveClick = {
                                        // Eliminar la recomendación guardada
                                        viewModel.removeRecommendation(
                                            recommendation = savedRecommendation
                                        ) { message ->
                                            // Guardar el mensaje para procesarlo en el contexto @Composable
                                            lastRecommendationMessage = message
                                        }
                                    }
                                )

                                if (index < uiState.savedRecommendations.lastIndex) {
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
                    }
                }
            }
        }
    }
}
