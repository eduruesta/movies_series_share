package com.bebi.watchit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.analytics.AnalyticsManager
import com.bebi.watchit.ui.components.Arrow_back
import com.bebi.watchit.viewmodel.GroupsViewModel
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.create_group
import moviesseriesshare.composeapp.generated.resources.create_group_button
import moviesseriesshare.composeapp.generated.resources.description
import moviesseriesshare.composeapp.generated.resources.group_description
import moviesseriesshare.composeapp.generated.resources.group_name
import moviesseriesshare.composeapp.generated.resources.name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

data class CreateGroupScreen(
    val userId: String,
    val userName: String = "Usuario",
    val userEmail: String = ""
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }

        val viewModel = koinInject<GroupsViewModel> { parametersOf(userId, userName, userEmail) }

        // Trackear vista de pantalla
        AnalyticsManager.trackScreenView(Firebase.analytics, "Create Group Screen")

        var groupName by remember { mutableStateOf("") }
        var groupDescription by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var hasAttemptedCreate by remember { mutableStateOf(false) }

        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(uiState.groups, uiState.isLoading) {
            if (!uiState.isLoading && isLoading && uiState.error == null) {
                // Si ha terminado de cargar sin errores, volvemos a la pantalla anterior
                navigator.pop()
                navigator.push(
                    GroupOpinionList(
                        uiState.groups.last { it.createdBy == userId },
                        openGroupInfo = true
                    )
                )

                // Trackear éxito en la creación del grupo
                AnalyticsManager.trackUiElementClick(
                    Firebase.analytics,
                    elementName = "group_creation_success",
                    screenName = "Create Group Screen"
                )
            }
            isLoading = uiState.isLoading
        }

        CreateGroupContent(
            groupName = groupName,
            onGroupNameChange = { groupName = it },
            groupDescription = groupDescription,
            onGroupDescriptionChange = { groupDescription = it },
            onCreateGroup = {
                hasAttemptedCreate = true
                if (groupName.isEmpty() && groupDescription.isEmpty()) {
                    // No hacemos nada, se mostrará el error
                } else {
                    isLoading = true
                    viewModel.createGroup(groupName, groupDescription)
                }
            },
            onBackPressed = {
                navigator.pop()
                // Trackear clic en botón de volver
                AnalyticsManager.trackUiElementClick(
                    Firebase.analytics,
                    elementName = "back_button",
                    screenName = "Create Group Screen"
                )
            },
            showNameError = hasAttemptedCreate && groupName.isEmpty(),
            isLoading = isLoading,
            error = uiState.error,
            snackbarHostState = snackbarHostState,
            showDescriptionError = hasAttemptedCreate && groupDescription.isEmpty()
        )

        LaunchedEffect(uiState.error) {
            if (uiState.error != null) {
                snackbarHostState.showSnackbar(uiState.error ?: "Error")
                isLoading = false
                // Trackear error en la creación del grupo
                AnalyticsManager.trackError(
                    Firebase.analytics,
                    errorType = "group_creation_error",
                    errorMessage = uiState.error ?: "Unknown error",
                    screenName = "Create Group Screen"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupContent(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    groupDescription: String,
    onGroupDescriptionChange: (String) -> Unit,
    onCreateGroup: () -> Unit,
    onBackPressed: () -> Unit,
    showNameError: Boolean,
    showDescriptionError: Boolean,
    isLoading: Boolean,
    error: String?,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error)
        }
    }

    val isNameValid = groupName.isNotBlank()
    val isDescriptionValid = groupDescription.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.create_group)) },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackPressed()
                        // Trackear clic en botón de volver en la topbar
                        AnalyticsManager.trackUiElementClick(
                            Firebase.analytics,
                            elementName = "topbar_back_button",
                            screenName = "Create Group Screen"
                        )
                    }) {
                        Icon(
                            imageVector = Arrow_back,
                            contentDescription = stringResource(Res.string.back_button)
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AdaptiveCircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = onGroupNameChange,
                        label = { Text(stringResource(Res.string.group_name)) },
                        placeholder = { Text(stringResource(Res.string.name)) },
                        isError = showNameError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = onGroupDescriptionChange,
                        label = { Text(stringResource(Res.string.group_description)) },
                        placeholder = { Text(stringResource(Res.string.description)) },
                        isError = showDescriptionError,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isNameValid && isDescriptionValid) {
                                    onCreateGroup()
                                }
                            }
                        ),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onCreateGroup()
                            // Trackear intento de creación de grupo
                            AnalyticsManager.trackUiElementClick(
                                Firebase.analytics,
                                elementName = "create_group_button",
                                screenName = "Create Group Screen"
                            )
                        },
                        enabled = isNameValid && isDescriptionValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.create_group_button))
                    }
                }
            }
        }
    }
}
