package com.example.blesearchum.ui

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleTranslation
import com.example.blesearchum.model.BibleVerse
import com.example.blesearchum.model.FavoriteVerse

/**
 * Estado inmutable de la interfaz de usuario para BibleSearchUM.
 */
data class BibleUiState(
    val books: List<BibleBook> = emptyList(),
    val availableChapters: List<Int> = emptyList(),
    val availableVerses: List<Int> = emptyList(),
    val selectedBook: BibleBook? = null,
    val selectedChapter: Int? = null,
    val selectedVerse: Int? = null,
    val currentVerseContent: BibleVerse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Reto Adicional 3: Traducción bíblica activa y disponibles
    val selectedTranslation: String = "RV1960",
    val availableTranslations: List<BibleTranslation> = listOf(
        BibleTranslation("RV1960", "Reina-Valera 1960 (RV1960)"),
        BibleTranslation("NVI", "Nueva Versión Internacional (NVI)"),
        BibleTranslation("LBLA", "La Biblia de las Américas (LBLA)"),
        BibleTranslation("NTBT", "Santa Biblia NTBT")
    ),

    // Reto Adicional 2: Lista de favoritos y estado del modal
    val favorites: List<FavoriteVerse> = emptyList(),
    val isCurrentVerseFavorite: Boolean = false,
    val showFavoritesSheet: Boolean = false,
    val userMessage: String? = null
)
