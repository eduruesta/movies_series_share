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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.ui.components.Info
import com.bebi.watchit.ui.components.MediaOpinionItem
import com.bebi.watchit.ui.components.RatingBottomSheet
import com.bebi.watchit.ui.components.copyToClipboard
import com.bebi.watchit.viewmodel.GroupDetailUiState
import com.bebi.watchit.viewmodel.GroupDetailViewModel
import com.bebi.watchit.viewmodel.GroupsViewModel
import com.bebi.watchit.viewmodel.MediaOpinionFormViewModel
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.add_new_comment
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel_button
import moviesseriesshare.composeapp.generated.resources.close
import moviesseriesshare.composeapp.generated.resources.copy_code
import moviesseriesshare.composeapp.generated.resources.delete_button
import moviesseriesshare.composeapp.generated.resources.delete_group
import moviesseriesshare.composeapp.generated.resources.delete_group_confirmation
import moviesseriesshare.composeapp.generated.resources.group_invite_code
import moviesseriesshare.composeapp.generated.resources.leave_button
import moviesseriesshare.composeapp.generated.resources.leave_group
import moviesseriesshare.composeapp.generated.resources.leave_group_confirmation
import moviesseriesshare.composeapp.generated.resources.leave_group_owner_message
import moviesseriesshare.composeapp.generated.resources.leave_group_title
import moviesseriesshare.composeapp.generated.resources.members
import moviesseriesshare.composeapp.generated.resources.without_critics
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class GroupOpinionList(private val group: GroupResponse) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val auth = remember { Firebase.auth }
        val firebaseUser: FirebaseUser? by remember { mutableStateOf(auth.currentUser) }

        val viewModel = koinInject<GroupDetailViewModel> { parametersOf(group.id) }
        val opinionViewModel = koinInject<MediaOpinionFormViewModel>()
        val mediaOpinionViewModel = koinInject<MediaOpinionViewModel>()
        val groupsViewModel = koinInject<GroupsViewModel> {
            parametersOf(
                firebaseUser?.uid ?: "",
                firebaseUser?.displayName ?: "Usuario",
                firebaseUser?.email ?: ""
            )
        }

        val uiState by viewModel.uiState.collectAsState()

        GroupOpinionListScreen(
            uiState = uiState,
            groupName = group.name,
            inviteCode = group.inviteCode,
            onBackPressed = { navigator.pop() },
            onOpinionClick = { opinion -> navigator.push(MediaDetailScreen(opinion.id)) },
            onAddOpinionClick = {
                opinionViewModel.setGroupIdForNextSave(group.id)
                navigator.push(OpinionFormScreen(group.id))
            },
            groupInfo = group,
            groupsViewModel = groupsViewModel,
            onLeaveGroup = {
                groupsViewModel.leaveGroup(group.id)
                navigator.pop()
            },
            onDeleteGroup = {
                groupsViewModel.deleteGroup(group.id)
                navigator.pop()
            },
            mediaOpinionViewModel = mediaOpinionViewModel,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupOpinionListScreen(
    uiState: GroupDetailUiState,
    groupName: String,
    inviteCode: String,
    onBackPressed: () -> Unit,
    onOpinionClick: (MediaOpinion) -> Unit,
    onAddOpinionClick: () -> Unit,
    groupInfo: GroupResponse,
    groupsViewModel: GroupsViewModel,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    mediaOpinionViewModel: MediaOpinionViewModel,
    viewModel: GroupDetailViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRatingSheet by remember { mutableStateOf(false) }
    var selectedOpinion by remember { mutableStateOf<MediaOpinion?>(null) }
    var showGroupInfoDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back_button)
                        )
                    }
                },
                actions = {
                    if (inviteCode.isNotBlank()) {
                        IconButton(onClick = { showGroupInfoDialog = true }) {
                            Icon(
                                imageVector = Info,
                                contentDescription = "Info"
                            )
                        }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddOpinionClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_new_comment)
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
                    EmptyOpinionsMessage()
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.opinions) { opinion ->
                            MediaOpinionItem(
                                opinion = opinion,
                                onClick = { onOpinionClick(opinion) },
                                onRateClick = {
                                    selectedOpinion = opinion
                                    showRatingSheet = true
                                },
                                onShowMessage = { message ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRatingSheet && selectedOpinion != null) {
        var isLoading by remember { mutableStateOf(false) }

        RatingBottomSheet(
            opinion = selectedOpinion!!,
            onDismiss = { showRatingSheet = false },
            isLoading = isLoading,
            onRatingSubmit = { opinion, rating ->
                isLoading = true
                mediaOpinionViewModel.submitRating(opinion, rating) { success ->
                    isLoading = false
                    if (success) {
                        viewModel.refreshOpinions()
                        showRatingSheet = false
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("No se pudo actualizar la calificación")
                        }
                    }
                }
            }
        )
    }

    if (showGroupInfoDialog) {
        val isOwner = groupsViewModel.isGroupOwner(groupInfo)
        GroupInfoDialog(
            groupName = groupName,
            inviteCode = inviteCode,
            onDismiss = { showGroupInfoDialog = false },
            clipboardManager = clipboardManager,
            groupInfo = groupInfo,
            isOwner = isOwner,
            onLeaveGroup = onLeaveGroup,
            onDeleteGroup = onDeleteGroup
        )
    }
}

@Composable
private fun GroupInfoDialog(
    groupName: String,
    inviteCode: String,
    onDismiss: () -> Unit,
    clipboardManager: ClipboardManager,
    groupInfo: GroupResponse,
    isOwner: Boolean,
    onLeaveGroup: () -> Unit = {},
    onDeleteGroup: () -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.group_invite_code),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(inviteCode))
                            }
                        ) {
                            Icon(
                                imageVector = copyToClipboard,
                                contentDescription = stringResource(Res.string.copy_code)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.members),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        groupInfo.members.forEachIndexed { index, member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            if (index < groupInfo.members.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showLeaveConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.leave_group))
                }

                if (isOwner) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.delete_group))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.close))
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(Res.string.delete_group)) },
            text = {
                Text(stringResource(Res.string.delete_group_confirmation))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDismiss()
                        onDeleteGroup()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.delete_button))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(Res.string.cancel_button))
                }
            }
        )
    }

    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            title = { Text(stringResource(Res.string.leave_group_title)) },
            text = {
                Text(
                    if (isOwner)
                        stringResource(Res.string.leave_group_owner_message)
                    else stringResource(Res.string.leave_group_confirmation)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveConfirmation = false
                        onDismiss()
                        onLeaveGroup()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(stringResource(Res.string.leave_button))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLeaveConfirmation = false }) {
                    Text(stringResource(Res.string.cancel_button))
                }
            }
        )
    }
}

@Composable
private fun EmptyOpinionsMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.without_critics),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
