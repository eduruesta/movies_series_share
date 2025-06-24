package com.bebi.watchit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.model.MediaOpinion
import com.bebi.watchit.rememberShareManager
import com.bebi.watchit.ui.components.ErrorScreen
import com.bebi.watchit.ui.components.Info
import com.bebi.watchit.ui.components.MediaOpinionItem
import com.bebi.watchit.ui.components.RatingBottomSheet
import com.bebi.watchit.ui.components.ThreeDots
import com.bebi.watchit.viewmodel.GroupDetailUiState
import com.bebi.watchit.viewmodel.GroupDetailViewModel
import com.bebi.watchit.viewmodel.GroupsViewModel
import com.bebi.watchit.viewmodel.MediaOpinionFormViewModel
import com.bebi.watchit.viewmodel.MediaOpinionViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.add_new_comment
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel_button
import moviesseriesshare.composeapp.generated.resources.close
import moviesseriesshare.composeapp.generated.resources.copy_code
import moviesseriesshare.composeapp.generated.resources.create_critic_button
import moviesseriesshare.composeapp.generated.resources.delete_button
import moviesseriesshare.composeapp.generated.resources.delete_group
import moviesseriesshare.composeapp.generated.resources.delete_group_confirmation
import moviesseriesshare.composeapp.generated.resources.group_invite_code
import moviesseriesshare.composeapp.generated.resources.invite_code_message
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

data class GroupOpinionList(private val group: GroupResponse, val openGroupInfo: Boolean = false) :
    Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val auth = remember { Firebase.auth }
        val firebaseUser: FirebaseUser? by remember { mutableStateOf(auth.currentUser) }

        val viewModel = koinInject<GroupDetailViewModel> { parametersOf(group.id) }
        val opinionViewModel = koinInject<MediaOpinionFormViewModel>()
        val userId = firebaseUser?.uid ?: "anonymous_user"
        val mediaOpinionViewModel: MediaOpinionViewModel = koinInject { parametersOf(userId) }
        val groupsViewModel = koinInject<GroupsViewModel> {
            parametersOf(
                firebaseUser?.uid ?: "",
                firebaseUser?.displayName ?: "Usuario",
                firebaseUser?.email ?: ""
            )
        }

        val scope = rememberCoroutineScope()
        val uiState by viewModel.uiState.collectAsState()

        // Forzar la actualización de las opiniones cada vez que se navega a esta pantalla
        LaunchedEffect(Unit) {
            viewModel.refreshOpinions()
        }

        GroupOpinionListScreen(
            uiState = uiState,
            groupName = group.name,
            onBackPressed = { navigator.pop() },
            onOpinionClick = { opinion -> navigator.push(MediaDetailScreen(opinion.id)) },
            onAddOpinionClick = {
                opinionViewModel.setGroupIdForNextSave(group.id)
                val username = firebaseUser?.displayName ?: "Usuario"
                opinionViewModel.setUsernameForNextSave(username)
                navigator.push(OpinionFormScreen(group.id))
            },
            groupInfo = group,
            groupsViewModel = groupsViewModel,
            onLeaveGroup = {
                scope.launch {
                    groupsViewModel.leaveGroup(group.id)
                    delay(400)
                    navigator.pop()
                }
            },
            onDeleteGroup = {
                scope.launch {
                    groupsViewModel.deleteGroup(group.id)
                    delay(400)
                    navigator.pop()
                }
            },
            mediaOpinionViewModel = mediaOpinionViewModel,
            viewModel = viewModel,
            openGroupInfo
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupOpinionListScreen(
    uiState: GroupDetailUiState,
    groupName: String,
    onBackPressed: () -> Unit,
    onOpinionClick: (MediaOpinion) -> Unit,
    onAddOpinionClick: () -> Unit,
    groupInfo: GroupResponse,
    groupsViewModel: GroupsViewModel,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    mediaOpinionViewModel: MediaOpinionViewModel,
    viewModel: GroupDetailViewModel,
    openGroupInfo: Boolean
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRatingSheet by remember { mutableStateOf(false) }
    var selectedOpinion by remember { mutableStateOf<MediaOpinion?>(null) }
    var showGroupInfoDialog by remember { mutableStateOf(false) }
    var showGroupInfo by remember { mutableStateOf(openGroupInfo) }


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

                    IconButton(onClick = { showGroupInfoDialog = true }) {
                        Icon(
                            imageVector = ThreeDots,
                            contentDescription = "Info"
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Contenido principal con padding para dejar espacio al botón CTA en la parte inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 80.dp) // Espacio para el botón CTA
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.error != null -> {
                        ErrorScreen(
                            onRetry = { viewModel.refreshOpinions() }
                        )
                    }

                    uiState.opinions.isEmpty() -> {
                        EmptyOpinionsMessage()
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
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

            // Botón CTA fijo en la parte inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(1f)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        .background(Color.Transparent)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onAddOpinionClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.create_critic_button),
                            style = MaterialTheme.typography.labelLarge
                        )
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

    if (showGroupInfoDialog || showGroupInfo) {
        val isOwner = groupsViewModel.isGroupOwner(groupInfo)
        GroupInfoBottomSheet(
            groupInfo = groupInfo,
            onDismiss = {
                showGroupInfoDialog = false
                showGroupInfo = false
            },
            onLeaveGroup = onLeaveGroup,
            onDeleteGroup = onDeleteGroup,
            isOwner = isOwner,
            member = stringResource(
                Res.string.invite_code_message,
                groupInfo.inviteCode,
                groupInfo.name
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupInfoBottomSheet(
    groupInfo: GroupResponse,
    onDismiss: () -> Unit,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    isOwner: Boolean,
    member: String
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val shareManager = rememberShareManager()
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val inviteCode = groupInfo.inviteCode

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título del grupo
            item {
                Text(
                    text = groupInfo.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Código de invitación
            item {
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
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            shareManager.shareText(member)
                        }
                    }
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
                            style = MaterialTheme.typography.titleLarge
                        )

                        IconButton(
                            onClick = {
                                scope.launch {
                                    shareManager.shareText(member)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(Res.string.copy_code)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Título de miembros
            item {
                Text(
                    text = stringResource(Res.string.members),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tarjeta con miembros (usamos directamente los ítems en la LazyColumn principal)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botón salir del grupo
            item {
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
            }

            // Botón eliminar grupo (solo para propietarios)
            if (isOwner) {
                item {
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
            }

            // Botón cerrar
            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.close))
                }

                // Espaciado extra para evitar que el contenido quede bajo gestos de navegación
                Spacer(modifier = Modifier.height(8.dp))
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
                        onDeleteGroup()
                        showDeleteConfirmation = false
                        onDismiss()
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
                        onLeaveGroup()
                        showLeaveConfirmation = false
                        onDismiss()
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
