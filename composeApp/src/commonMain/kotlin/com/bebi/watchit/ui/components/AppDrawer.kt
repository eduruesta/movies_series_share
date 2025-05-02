package com.bebi.watchit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bebi.watchit.theme.LocalThemeIsDark
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.app_name
import moviesseriesshare.composeapp.generated.resources.groups
import moviesseriesshare.composeapp.generated.resources.media_list_title
import moviesseriesshare.composeapp.generated.resources.movies
import moviesseriesshare.composeapp.generated.resources.series
import moviesseriesshare.composeapp.generated.resources.settings
import moviesseriesshare.composeapp.generated.resources.top_movies
import moviesseriesshare.composeapp.generated.resources.top_series
import moviesseriesshare.composeapp.generated.resources.trending
import moviesseriesshare.composeapp.generated.resources.upcoming
import moviesseriesshare.composeapp.generated.resources.your_recommendations
import org.jetbrains.compose.resources.stringResource

/**
 * Drawer content for the application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(
    onNavigateToMediaList: () -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToTopSeries: () -> Unit,
    onNavigateToTrendingSeries: () -> Unit,
    onNavigateToUpcomingMovies: () -> Unit,
    onNavigateToTopMovies: () -> Unit,
    onNavigateToTrendingMovies: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.85f),
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(12.dp)
        )

        HorizontalDivider(color = Color(0xFF38444D))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

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
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

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
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.groups)) },
                selected = false,
                onClick = {
                    onNavigateToGroups()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            // Series
            Text(
                text = stringResource(Res.string.series),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.top_series)) },
                selected = false,
                onClick = {
                    onNavigateToTopSeries()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.trending)) },
                selected = false,
                onClick = {
                    onNavigateToTrendingSeries()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            // Movies
            Text(
                text = stringResource(Res.string.movies),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.top_movies)) },
                selected = false,
                onClick = {
                    onNavigateToTopMovies()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.trending)) },
                selected = false,
                onClick = {
                    onNavigateToTrendingMovies()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                label = { Text(stringResource(Res.string.upcoming)) },
                selected = false,
                onClick = {
                    onNavigateToUpcomingMovies()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
        

        HorizontalDivider(color = Color(0xFF38444D))
        
        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            label = { Text(stringResource(Res.string.settings)) },
            selected = false,
            onClick = {
                onNavigateToSettings()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}
