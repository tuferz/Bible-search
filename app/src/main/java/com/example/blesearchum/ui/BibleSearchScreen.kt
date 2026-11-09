package com.example.blesearchum.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blesearchum.ui.components.DropdownSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSearchScreen(viewModel: BibleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    // Helper para compartir versículo mediante Intent implícito (Reto 1)
    fun shareVerse() {
        val verseData = uiState.currentVerseContent ?: return
        val bookName = uiState.selectedBook?.name ?: "Biblia"
        val chapter = uiState.selectedChapter ?: 1
        val shareText = "«${verseData.text.trim()}»\n— $bookName $chapter:${verseData.verse} (${uiState.selectedTranslation})"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir versículo")
        context.startActivity(shareIntent)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "BibleSearchUM",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Reto 2: Acceso directo a Favoritos
                    IconButton(onClick = { viewModel.setShowFavoritesSheet(true) }) {
                        Icon(
                            imageVector = Icons.Default.Bookmarks,
                            contentDescription = "Ver Favoritos",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reiniciar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Reto 1: FAB para compartir versículo activo si está disponible
            if (uiState.currentVerseContent != null) {
                FloatingActionButton(
                    onClick = { shareVerse() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir Versículo"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reto 3: Selector de Traducción Bíblica
            DropdownSelector(
                label = "Traducción",
                options = uiState.availableTranslations,
                selectedItem = uiState.availableTranslations.find { it.code == uiState.selectedTranslation },
                onItemSelected = { viewModel.onTranslationSelected(it.code) },
                itemLabelProvider = { it.name }
            )

            // Controles de Selección Principales
            DropdownSelector(
                label = "Libro",
                options = uiState.books,
                selectedItem = uiState.selectedBook,
                onItemSelected = { viewModel.onBookSelected(it) },
                itemLabelProvider = { it.name }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownSelector(
                    label = "Capítulo",
                    options = uiState.availableChapters,
                    selectedItem = uiState.selectedChapter,
                    onItemSelected = { viewModel.onChapterSelected(it) },
                    itemLabelProvider = { it.toString() },
                    enabled = uiState.selectedBook != null,
                    modifier = Modifier.weight(1f)
                )

                DropdownSelector(
                    label = "Versículo",
                    options = uiState.availableVerses,
                    selectedItem = uiState.selectedVerse,
                    onItemSelected = { viewModel.onVerseSelected(it) },
                    itemLabelProvider = { it.toString() },
                    enabled = uiState.selectedChapter != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicadores de Estado
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Visualización del Versículo
            uiState.currentVerseContent?.let { verseData ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.selectedBook?.name} ${uiState.selectedChapter}:${verseData.verse}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.selectedTranslation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "«${verseData.text.trim()}»",
                            fontSize = 17.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones de acción del versículo (Reto 1 y Reto 2)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = { viewModel.toggleFavoriteCurrentVerse() }
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCurrentVerseFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (uiState.isCurrentVerseFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { shareVerse() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reto 2: ModalBottomSheet para explorar versículos guardados en Favoritos
        if (uiState.showFavoritesSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowFavoritesSheet(false) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Versículos Favoritos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (uiState.favorites.isEmpty()) {
                        Text(
                            text = "Aún no tienes versículos guardados. Haz clic en el icono del corazón para guardar uno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxHeight(0.6f)
                        ) {
                            items(uiState.favorites, key = { it.id }) { fav ->
                                Card(
                                    onClick = { viewModel.selectFavoriteVerse(fav) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${fav.bookName} ${fav.chapter}:${fav.verse} (${fav.translation})",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = fav.text.trim(),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeFavorite(fav.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar de favoritos",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
