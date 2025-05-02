package com.bebi.watchit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.viewmodel.GroupsUiState
import com.bebi.watchit.viewmodel.GroupsViewModel
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.create_group
import moviesseriesshare.composeapp.generated.resources.create_your_first_group
import moviesseriesshare.composeapp.generated.resources.group_members
import moviesseriesshare.composeapp.generated.resources.join_group
import moviesseriesshare.composeapp.generated.resources.my_groups
import moviesseriesshare.composeapp.generated.resources.no_groups
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class GroupsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val currentUsername = "Usuario"
        
        val viewModel = koinInject<GroupsViewModel> { parametersOf(currentUsername) }
        
        val uiState by viewModel.uiState.collectAsState()

        GroupsScreen(
            uiState = uiState,
            onBackPressed = { navigator.pop() },
            onCreateGroupClicked = { /* Implementar navegación a crear grupo */ },
            onJoinGroupClicked = { /* Implementar unirse a grupo */ },
            onGroupClicked = { /* Implementar navegar a detalle de grupo */ },
            onRetryLoadGroups = { viewModel.loadGroups() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsScreen(
    uiState: GroupsUiState,
    onBackPressed: () -> Unit,
    onCreateGroupClicked: () -> Unit,
    onJoinGroupClicked: () -> Unit,
    onGroupClicked: (GroupResponse) -> Unit,
    onRetryLoadGroups: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar errores en el Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { 
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.my_groups)) },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_button)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onJoinGroupClicked) {
                        Text(stringResource(Res.string.join_group))
                    }
                }
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClicked,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.create_group)
                )
            }
        }
    ) { paddingValues ->
        GroupsContent(
            paddingValues = paddingValues,
            uiState = uiState,
            onGroupClicked = onGroupClicked,
            onRetryLoadGroups = onRetryLoadGroups
        )
    }
}

@Composable
private fun GroupsContent(
    paddingValues: PaddingValues,
    uiState: GroupsUiState,
    onGroupClicked: (GroupResponse) -> Unit,
    onRetryLoadGroups: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.groups.isEmpty() -> {
                EmptyGroupsView(
                    modifier = Modifier.fillMaxSize(),
                    onRetryClick = onRetryLoadGroups
                )
            }
            else -> {
                GroupsList(
                    groups = uiState.groups,
                    onGroupClicked = onGroupClicked
                )
            }
        }
    }
}

@Composable
private fun GroupsList(
    groups: List<GroupResponse>,
    onGroupClicked: (GroupResponse) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(groups) { group ->
            GroupItem(
                name = group.name,
                memberCount = group.members.size,
                onClick = { onGroupClicked(group) }
            )
        }
    }
}

@Composable
private fun EmptyGroupsView(
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_groups),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.create_your_first_group),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupItem(
    name: String,
    memberCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.group_members, memberCount),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
