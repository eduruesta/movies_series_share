package com.bebi.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.app_name
import moviesseriesshare.composeapp.generated.resources.ic_launcher
import moviesseriesshare.composeapp.generated.resources.media_list_title
import moviesseriesshare.composeapp.generated.resources.your_recommendations
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Drawer content for the application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    onNavigateToMediaList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToSettings: () -> Unit,
    drawerState: DrawerState,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
    ) {
        // App title
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider()

        // Navigation items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Home
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Home, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.media_list_title)) },
                selected = false,
                onClick = {
                    onNavigateToMediaList()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Recommendations
            NavigationDrawerItem(
                icon = {
                    Icon(
                        bookmarkCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.your_recommendations)) },
                selected = false,
                onClick = {
                    onNavigateToRecommendations()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            /*            // Profile
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text(stringResource(Res.string.profile)) },
                            selected = false,
                            onClick = {
                                onNavigateToProfile()
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // Settings
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text(stringResource(Res.string.theme)) },
                            selected = false,
                            onClick = {
                                onNavigateToSettings()
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )*/
        }
    }
}
