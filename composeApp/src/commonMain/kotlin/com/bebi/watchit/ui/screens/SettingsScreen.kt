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
import dev.burnoo.compose.remembersetting.rememberStringSetting
import kotlinx.coroutines.delay
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.appearance
import moviesseriesshare.composeapp.generated.resources.back_button
import moviesseriesshare.composeapp.generated.resources.dark_mode
import moviesseriesshare.composeapp.generated.resources.english
import moviesseriesshare.composeapp.generated.resources.language
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
private fun ThemeSection() {
    val isDarkTheme = LocalThemeIsDark.current
    val isDark by isDarkTheme

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
                onCheckedChange = { isDarkTheme.value = it }
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
