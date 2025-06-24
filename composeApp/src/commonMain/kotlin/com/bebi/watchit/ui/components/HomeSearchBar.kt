package com.bebi.watchit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bebi.watchit.viewmodel.MediaOpinionFormViewModel
import moviesseriesshare.composeapp.generated.resources.Res
import moviesseriesshare.composeapp.generated.resources.search_where_movies_series
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    mediaOpinionFormViewModel: MediaOpinionFormViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        SearchBar(
            modifier = modifier
                .fillMaxWidth(),
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        mediaOpinionFormViewModel.searchMedia(it)
                    },
                    onSearch = {
                        mediaOpinionFormViewModel.searchMedia(searchQuery)
                    },
                    expanded = false, // ya no importa
                    onExpandedChange = {}, // noop
                    placeholder = {
                        Text(stringResource(Res.string.search_where_movies_series))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
            },
            expanded = false,
            onExpandedChange = {}, // noop
            content = {} // sin contenido expandido
        )
    }
}


