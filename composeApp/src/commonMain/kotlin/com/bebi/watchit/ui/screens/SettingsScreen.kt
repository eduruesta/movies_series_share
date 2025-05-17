package com.bebi.watchit.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bebi.watchit.data.domain.Localization
import com.bebi.watchit.data.myLang
import com.bebi.watchit.theme.LocalThemeIsDark
import com.bebi.watchit.ui.components.logout
import dev.burnoo.compose.remembersetting.rememberStringSetting
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.appearance
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.cancel
import moviesseriesshare.composeapp.generated.resources.confirm
import moviesseriesshare.composeapp.generated.resources.dark_mode
import moviesseriesshare.composeapp.generated.resources.english
import moviesseriesshare.composeapp.generated.resources.language
import moviesseriesshare.composeapp.generated.resources.logout
import moviesseriesshare.composeapp.generated.resources.logout_confirmation
import moviesseriesshare.composeapp.generated.resources.profile
import moviesseriesshare.composeapp.generated.resources.select_language
import moviesseriesshare.composeapp.generated.resources.settings
import moviesseriesshare.composeapp.generated.resources.spanish
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class SettingsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        var isLoading by remember { mutableStateOf(false) }
        var refreshTrigger by remember { mutableStateOf(0) }
        var currentLanguage by remember { mutableStateOf(myLang ?: "en") }
        val auth = remember { Firebase.auth }
        val scope = rememberCoroutineScope()
        var firebaseUser: FirebaseUser? by remember { mutableStateOf(auth.currentUser) }


        androidx.compose.runtime.key(refreshTrigger) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.settings)) },
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
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (!isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            if (firebaseUser != null) {
                                ProfileSection(
                                    email = firebaseUser?.email ?: "",
                                    username = firebaseUser?.displayName ?: ""
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            
                            ThemeSection()

                            Spacer(modifier = Modifier.height(24.dp))

                            LanguageSection(
                                currentLanguage = currentLanguage,
                                onLanguageChanged = { newLang ->
                                    currentLanguage = newLang
                                    isLoading = true
                                    refreshTrigger++
                                }
                            )
                            
                            if (firebaseUser != null) {
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                LogoutSection(
                                    onLogout = {
                                        scope.launch {
                                            auth.signOut()
                                            withContext(Dispatchers.Main) {
                                                firebaseUser = auth.currentUser
                                            }
                                            navigator.pop()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )

                        LaunchedEffect(refreshTrigger) {
                            delay(300)
                            isLoading = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    email: String,
    username: String
) {
    Text(
        text = stringResource(Res.string.profile),
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = if (username.isNotEmpty()) username else email.substringBefore("@"),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ThemeSection() {
    val isDarkTheme = LocalThemeIsDark.current
    val isDark by isDarkTheme
    
    // Add persistent setting for dark mode
    var savedIsDark by rememberStringSetting(
        key = "savedIsDarkMode",
        defaultValue = isDark.toString()
    )
    
    // Apply saved setting when component is launched
    LaunchedEffect(Unit) {
        val darkModeSetting = savedIsDark.toBoolean()
        if (isDark != darkModeSetting) {
            isDarkTheme.value = darkModeSetting
        }
    }

    Text(
        text = stringResource(Res.string.appearance),
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.dark_mode),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = isDark,
                onCheckedChange = { 
                    isDarkTheme.value = it
                    savedIsDark = it.toString()
                }
            )
        }
    }
}

@Composable
private fun LanguageSection(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit
) {
    val languages = listOf("en" to Res.string.english, "es" to Res.string.spanish)
    val localization = koinInject<Localization>()

    var languageIso by rememberStringSetting(
        key = "savedLanguageIso",
        defaultValue = currentLanguage
    )

    LaunchedEffect(currentLanguage) {
        if (currentLanguage != languageIso) {
            languageIso = currentLanguage
        }
    }

    Text(
        text = stringResource(Res.string.language),
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.select_language),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            languages.forEach { (langCode, langNameRes) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = langCode == currentLanguage,
                            onClick = {
                                if (langCode != currentLanguage) {
                                    languageIso = langCode
                                    localization.applyLanguage(langCode)
                                    onLanguageChanged(langCode)
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = langCode == currentLanguage,
                        onClick = {
                            if (langCode != currentLanguage) {
                                languageIso = langCode
                                localization.applyLanguage(langCode)
                                onLanguageChanged(langCode)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(langNameRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutSection(
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Text(
        text = stringResource(Res.string.logout),
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .selectable(
                    selected = false,
                    onClick = { showLogoutDialog = true },
                    role = Role.Button
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = logout,
                contentDescription = stringResource(Res.string.logout)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(Res.string.logout),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(stringResource(Res.string.logout))
            },
            text = {
                Text(stringResource(Res.string.logout_confirmation))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}
