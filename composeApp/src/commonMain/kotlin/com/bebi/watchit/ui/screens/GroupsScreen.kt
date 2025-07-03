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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.analytics.AnalyticsManager
import com.bebi.watchit.data.models.GroupResponse
import com.bebi.watchit.ui.components.Arrow_back
import com.bebi.watchit.ui.components.Email
import com.bebi.watchit.ui.components.GoogleSignIn
import com.bebi.watchit.ui.components.Person
import com.bebi.watchit.ui.components.groupAdd
import com.bebi.watchit.ui.components.passwordIcon
import com.bebi.watchit.viewmodel.GroupsUiState
import com.bebi.watchit.viewmodel.GroupsViewModel
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.accept
import moviesseriesshare.composeapp.generated.resources.already_have_account
import moviesseriesshare.composeapp.generated.resources.auth_error
import moviesseriesshare.composeapp.generated.resources.auth_subtitle
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel
import moviesseriesshare.composeapp.generated.resources.complete_fields
import moviesseriesshare.composeapp.generated.resources.create_account
import moviesseriesshare.composeapp.generated.resources.create_group_button
import moviesseriesshare.composeapp.generated.resources.create_your_first_group
import moviesseriesshare.composeapp.generated.resources.dont_have_account
import moviesseriesshare.composeapp.generated.resources.email
import moviesseriesshare.composeapp.generated.resources.group_invitation_code
import moviesseriesshare.composeapp.generated.resources.group_members
import moviesseriesshare.composeapp.generated.resources.group_members_plural
import moviesseriesshare.composeapp.generated.resources.join_group
import moviesseriesshare.composeapp.generated.resources.join_group_description
import moviesseriesshare.composeapp.generated.resources.join_success_message
import moviesseriesshare.composeapp.generated.resources.login
import moviesseriesshare.composeapp.generated.resources.login_success
import moviesseriesshare.composeapp.generated.resources.my_groups
import moviesseriesshare.composeapp.generated.resources.no_groups
import moviesseriesshare.composeapp.generated.resources.password
import moviesseriesshare.composeapp.generated.resources.register
import moviesseriesshare.composeapp.generated.resources.username
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class GroupsScreen(private val deepLinkInviteCode: String? = null) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val snackbarHostState = remember { SnackbarHostState() }
        var userEmail by remember { mutableStateOf("") }
        var userName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isRegistrationMode by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val auth = remember { Firebase.auth }
        var firebaseUser: FirebaseUser? by remember { mutableStateOf(auth.currentUser) }

        // Observamos cambios en el usuario y actualizamos el estado local
        LaunchedEffect(Unit) {
            auth.authStateChanged.collect { user ->
                firebaseUser = user
            }
        }

        val viewModel = koinInject<GroupsViewModel> {
            parametersOf(
                firebaseUser?.uid ?: "",
                firebaseUser?.displayName ?: "Usuario",
                firebaseUser?.email ?: ""
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        val alreadyHandledInviteCode = rememberSaveable { mutableStateOf(false) }
        var isLoggedInSuccess by remember { mutableStateOf(false) }

        var showJoinGroupSheet by remember {
            mutableStateOf(deepLinkInviteCode != null && !alreadyHandledInviteCode.value)
        }

        val prefilledCode by remember { mutableStateOf(deepLinkInviteCode ?: "") }

        val authErrorText = stringResource(Res.string.auth_error)
        val completeFieldsText = stringResource(Res.string.complete_fields)
        val joinGroupText = stringResource(Res.string.join_success_message)
        val loginSuccessTemplate = stringResource(Res.string.login_success)


        // Cargar grupos cuando cambia el usuario o cuando se monta la pantalla
        LaunchedEffect(firebaseUser) {
            if (firebaseUser != null) {
                viewModel.loadGroups()
                if (isLoggedInSuccess) {
                    // Mostrar mensaje de bienvenida después del login exitoso
                    val userName = firebaseUser?.displayName ?: firebaseUser?.email ?: "Usuario"
                    val welcomeMessage = loginSuccessTemplate.replace("%1\$s", userName)
                    snackbarHostState.showSnackbar(welcomeMessage)
                    isLoggedInSuccess = false
                }
            }
        }

        /*
                LaunchedEffect(Unit) {
                    viewModel.loadGroups()
                }
        */

        if (firebaseUser != null) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    GroupsTopBar(
                        onBackClicked = { navigator.pop() },
                        scrollBehavior = scrollBehavior,
                        onJoinGroupClicked = { showJoinGroupSheet = true },
                    )
                }

                ) { paddingValues ->
                GroupsContent(
                    paddingValues = paddingValues,
                    uiState = uiState,
                    onGroupClicked = { group -> navigator.push(GroupOpinionList(group)) },
                    onRetryLoadGroups = { viewModel.loadGroups() },
                )

                if (showJoinGroupSheet) {
                    JoinGroupBottomSheet(
                        onDismiss = {
                            showJoinGroupSheet = false
                            alreadyHandledInviteCode.value = true
                        },
                        onJoin = { invitationCode ->
                            coroutineScope.launch {
                                showJoinGroupSheet = false
                                alreadyHandledInviteCode.value = true
                                viewModel.joinGroup(invitationCode)
                                snackbarHostState.showSnackbar(joinGroupText)
                                viewModel.loadGroups()
                            }
                        },
                        prefilledCode = prefilledCode
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                                onClick = {
                                    firebaseUser?.let { user ->
                                        navigator.push(
                                            CreateGroupScreen(
                                                userId = user.uid,
                                                userName = user.displayName ?: "Usuario",
                                                userEmail = user.email ?: ""
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = stringResource(Res.string.create_group_button),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.my_groups)) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    imageVector = Arrow_back,
                                    contentDescription = stringResource(Res.string.back_button)
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isRegistrationMode) stringResource(Res.string.create_account) else stringResource(
                                Res.string.login
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.auth_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isRegistrationMode) {
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text(stringResource(Res.string.username)) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Person,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        OutlinedTextField(
                            value = userEmail,
                            onValueChange = { userEmail = it },
                            label = { Text(stringResource(Res.string.email)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(Res.string.password)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(
                                    imageVector = passwordIcon,
                                    contentDescription = null
                                )
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val isFormValid = if (isRegistrationMode) {
                                    password.isNotBlank() && userEmail.isNotBlank() && userName.isNotBlank()
                                } else {
                                    password.isNotBlank() && userEmail.isNotBlank()
                                }

                                if (isFormValid) {
                                    coroutineScope.launch {
                                        try {
                                            if (isRegistrationMode) {
                                                val userCredential =
                                                    auth.createUserWithEmailAndPassword(
                                                        email = userEmail,
                                                        password = password
                                                    )

                                                userCredential.user?.updateProfile(
                                                    displayName = userName
                                                )
                                            } else {
                                                auth.signInWithEmailAndPassword(
                                                    email = userEmail,
                                                    password = password
                                                )
                                            }

                                            withContext(Dispatchers.Main) {
                                                isLoggedInSuccess = true
                                                firebaseUser = auth.currentUser
                                                val method = if (isRegistrationMode) "register" else "email"
                                                AnalyticsManager.trackLoginSuccess(Firebase.analytics, method)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                snackbarHostState.showSnackbar(authErrorText)
                                                val method = if (isRegistrationMode) "register" else "email"
                                                AnalyticsManager.trackLoginError(Firebase.analytics, method, e.message ?: "Unknown error")
                                            }
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(completeFieldsText)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (isRegistrationMode) stringResource(Res.string.register) else stringResource(
                                    Res.string.login
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val onFirebaseResult: (Result<FirebaseUser?>) -> Unit = { result ->
                            if (result.isSuccess) {
                                isLoggedInSuccess = true
                                val firebase = result.getOrNull()
                                firebaseUser = firebase
                                AnalyticsManager.trackLoginSuccess(Firebase.analytics, "google")
                            } else {
                                println("Error Result: ${result.exceptionOrNull()?.message}")
                                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                                AnalyticsManager.trackLoginError(Firebase.analytics, "google", errorMessage)
                            }

                        }
                        GoogleSignIn(onFirebaseResult = onFirebaseResult)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isRegistrationMode) stringResource(Res.string.already_have_account) else stringResource(
                                Res.string.dont_have_account
                            ),
                            modifier = Modifier.clickable {
                                isRegistrationMode = !isRegistrationMode
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinGroupBottomSheet(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit,
    prefilledCode: String = ""
) {
    val sheetState = rememberModalBottomSheetState()
    var invitationCode by remember(prefilledCode) { mutableStateOf(prefilledCode) }

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
                    imageVector = Arrow_back,
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
    onRetryLoadGroups: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                AdaptiveCircularProgressIndicator()
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
        contentPadding = PaddingValues(
            top = 8.dp, 
            start = 8.dp, 
            end = 8.dp,
            bottom = 80.dp // Añadir espacio adicional en la parte inferior para el botón
        )
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
                text = if (memberCount == 1) {
                    stringResource(Res.string.group_members, memberCount)
                } else {
                    stringResource(Res.string.group_members_plural, memberCount)
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
