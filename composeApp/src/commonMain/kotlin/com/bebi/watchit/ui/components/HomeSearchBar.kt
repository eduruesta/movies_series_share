package com.bebi.watchit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bebi.watchit.analytics.AnalyticsManager
import com.bebi.watchit.viewmodel.MediaOpinionFormViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.search_where_movies_series
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    mediaOpinionFormViewModel: MediaOpinionFormViewModel
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
            },
            label = { Text(stringResource(Res.string.search_where_movies_series)) },
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    mediaOpinionFormViewModel.searchMedia(searchQuery)
                    keyboardController?.hide()
                    AnalyticsManager.trackMediaSearch(Firebase.analytics, searchQuery, 0) // El conteo se actualizará cuando lleguen los resultados
                }
            )
        )

        IconButton(
            onClick = {
                keyboardController?.hide()
                mediaOpinionFormViewModel.searchMedia(searchQuery)
                AnalyticsManager.trackMediaSearch(Firebase.analytics, searchQuery, 0) // El conteo se actualizará cuando lleguen los resultados
            },
            modifier = Modifier.align(Alignment.Bottom).height(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            if (mediaOpinionFormViewModel.isSearching) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
