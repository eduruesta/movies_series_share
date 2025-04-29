package com.bebi.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Barra de aplicación superior con funcionalidad de búsqueda.
 * Esta barra muestra un título y un ícono de búsqueda. Al hacer clic en el ícono,
 * se anima para mostrar un campo de búsqueda que ocupa todo el ancho de la barra.
 *
 * @param title El título a mostrar cuando la búsqueda no está activa
 * @param navigationIcon El ícono de navegación (generalmente un menú o flecha atrás)
 * @param onNavigationIconClick Acción a ejecutar cuando se hace clic en el ícono de navegación
 * @param onSearchQueryChanged Callback que se llama cuando cambia el texto de búsqueda
 * @param scrollBehavior Comportamiento de desplazamiento para la barra superior
 * @param placeHolderText Texto de marcador de posición para el campo de búsqueda
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    title: String,
    navigationIcon: @Composable () -> Unit,
    onNavigationIconClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    placeHolderText: String = "Buscar..."
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(title)
                }
                
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                    exit = fadeOut() + slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing
                        )
                    )
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            onSearchQueryChanged(it.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text(placeHolderText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                // Opcionalmente podría ejecutar una acción específica al presionar Search
                                onSearchQueryChanged(searchQuery.text)
                            }
                        ),
                        leadingIcon = {
                            IconButton(onClick = {
                                if (isSearchActive) {
                                    isSearchActive = false
                                    searchQuery = TextFieldValue("")
                                    onSearchQueryChanged("")
                                } else {
                                    onNavigationIconClick()
                                }
                            }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                                    contentDescription = if (isSearchActive) "Cerrar búsqueda" else "Abrir menú"
                                )
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.text.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = TextFieldValue("")
                                        onSearchQueryChanged("")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                }
            }
        },
        navigationIcon = {
            if (!isSearchActive) {
                navigationIcon()
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = {
                    isSearchActive = true
                    // Solicitar el foco al campo de texto después de que la animación comience
                    // para evitar problemas con el teclado
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
