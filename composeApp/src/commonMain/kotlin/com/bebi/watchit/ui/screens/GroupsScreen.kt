package com.bebi.watchit.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.ui.components.groupAdd
import com.bebi.watchit.viewmodel.GroupsUiState
import com.bebi.watchit.viewmodel.GroupsViewModel
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.accept
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel
import moviesseriesshare.composeapp.generated.resources.create_group
import moviesseriesshare.composeapp.generated.resources.create_your_first_group
import moviesseriesshare.composeapp.generated.resources.group_invitation_code
import moviesseriesshare.composeapp.generated.resources.group_members
import moviesseriesshare.composeapp.generated.resources.join_group
import moviesseriesshare.composeapp.generated.resources.join_group_description
import moviesseriesshare.composeapp.generated.resources.my_groups
import moviesseriesshare.composeapp.generated.resources.no_groups
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class GroupsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val viewModel = koinInject<GroupsViewModel> { parametersOf("username") }
        val uiState by viewModel.uiState.collectAsState()

        var showJoinGroupSheet by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GroupsTopBar(
                    onBackClicked = { navigator.pop() },
                    scrollBehavior = scrollBehavior,
                    onJoinGroupClicked = { showJoinGroupSheet = true },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(CreateGroupScreen()) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                onGroupClicked = { group -> navigator.push(GroupOpinionList(group)) },
                onRetryLoadGroups = { viewModel.loadGroups() }
            )

            if (showJoinGroupSheet) {
                JoinGroupBottomSheet(
                    onDismiss = { showJoinGroupSheet = false },
                    onJoin = { invitationCode ->
                        // Aquí se implementará la lógica para unirse al grupo
                        coroutineScope.launch {
                            // Simular unirse al grupo
                            showJoinGroupSheet = false
                            snackbarHostState.showSnackbar("Unido al grupo con código: $invitationCode")
                            // Eventualmente esto llamaría a viewModel.joinGroup(invitationCode)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinGroupBottomSheet(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var invitationCode by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.join_group),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.join_group_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = invitationCode,
                onValueChange = { invitationCode = it },
                label = { Text(stringResource(Res.string.group_invitation_code)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Button(
                    onClick = { onJoin(invitationCode) },
                    enabled = invitationCode.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.accept))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsTopBar(
    onBackClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    onJoinGroupClicked: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.my_groups)) },
        navigationIcon = {
            IconButton(onClick = { onBackClicked() }) {
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
                Icon(
                    imageVector = groupAdd,
                    contentDescription = stringResource(Res.string.join_group)
                )
            }
        }
    )
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
                description = group.description,
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
    description: String,
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
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.group_members, memberCount),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
