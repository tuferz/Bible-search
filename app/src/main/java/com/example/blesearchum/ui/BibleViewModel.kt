package com.example.blesearchum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blesearchum.data.FavoritesRepository
import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.FavoriteVerse
import com.example.blesearchum.network.BibleApiService
import com.example.blesearchum.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BibleViewModel(
    private val favoritesRepository: FavoritesRepository? = null,
    private val apiService: BibleApiService = RetrofitInstance.api
) : ViewModel() {

    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        fetchBooks()
        observeFavorites()
    }

    private fun observeFavorites() {
        favoritesRepository?.let { repo ->
            viewModelScope.launch {
                repo.favoritesFlow.collect { favList ->
                    _uiState.update { current ->
                        current.copy(
                            favorites = favList,
                            isCurrentVerseFavorite = checkIfCurrentIsFavorite(current, favList)
                        )
                    }
                }
            }
        }
    }

    private fun checkIfCurrentIsFavorite(state: BibleUiState, favList: List<FavoriteVerse>): Boolean {
        val currentBook = state.selectedBook ?: return false
        val currentChapter = state.selectedChapter ?: return false
        val currentVerse = state.selectedVerse ?: return false
        val id = "${state.selectedTranslation}-${currentBook.bookId}-$currentChapter-$currentVerse"
        return favList.any { it.id == id }
    }

    fun fetchBooks(translation: String = _uiState.value.selectedTranslation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookList = apiService.getBooksByTranslation(translation)
                _uiState.update {
                    it.copy(
                        books = bookList,
                        isLoading = false,
                        selectedTranslation = translation
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar libros: ${e.localizedMessage ?: e.message}"
                    )
                }
            }
        }
    }

    fun onBookSelected(book: BibleBook) {
        val chaptersList = (1..book.chapters).toList()
        _uiState.update {
            it.copy(
                selectedBook = book,
                availableChapters = chaptersList,
                selectedChapter = null,
                selectedVerse = null,
                availableVerses = emptyList(),
                currentVerseContent = null,
                isCurrentVerseFavorite = false
            )
        }
    }

    fun onChapterSelected(chapter: Int) {
        // Asumimos un máximo genérico de 150 versículos por capítulo para consulta
        val versesList = (1..150).toList()
        _uiState.update {
            it.copy(
                selectedChapter = chapter,
                availableVerses = versesList,
                selectedVerse = null,
                currentVerseContent = null,
                isCurrentVerseFavorite = false
            )
        }
    }

    fun onVerseSelected(verse: Int) {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: return
        val currentTranslation = _uiState.value.selectedTranslation

        _uiState.update { it.copy(selectedVerse = verse, isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = apiService.getVerseByTranslation(
                    currentTranslation,
                    currentBook.bookId,
                    currentChapter,
                    verse
                )
                _uiState.update { current ->
                    val id = "${currentTranslation}-${currentBook.bookId}-$currentChapter-$verse"
                    val isFav = current.favorites.any { it.id == id }
                    current.copy(
                        currentVerseContent = result,
                        isLoading = false,
                        isCurrentVerseFavorite = isFav
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo obtener el versículo solicitado."
                    )
                }
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            BibleUiState(
                books = it.books,
                selectedTranslation = it.selectedTranslation,
                favorites = it.favorites
            )
        }
    }

    // Reto Adicional 3: Cambio de traducción bíblica
    fun onTranslationSelected(newTranslation: String) {
        if (newTranslation == _uiState.value.selectedTranslation) return
        _uiState.update {
            it.copy(
                selectedTranslation = newTranslation,
                selectedBook = null,
                selectedChapter = null,
                selectedVerse = null,
                availableChapters = emptyList(),
                availableVerses = emptyList(),
                currentVerseContent = null
            )
        }
        fetchBooks(newTranslation)
    }

    // Reto Adicional 2: Métodos de gestión de favoritos
    fun toggleFavoriteCurrentVerse() {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: return
        val currentVerse = _uiState.value.selectedVerse ?: return
        val currentContent = _uiState.value.currentVerseContent ?: return
        val currentTrans = _uiState.value.selectedTranslation
        val id = "$currentTrans-${currentBook.bookId}-$currentChapter-$currentVerse"

        val repo = favoritesRepository ?: return

        viewModelScope.launch {
            if (_uiState.value.isCurrentVerseFavorite) {
                repo.removeFavorite(id)
                _uiState.update { it.copy(userMessage = "Eliminado de favoritos") }
            } else {
                val fav = FavoriteVerse(
                    id = id,
                    translation = currentTrans,
                    bookName = currentBook.name,
                    bookId = currentBook.bookId,
                    chapter = currentChapter,
                    verse = currentVerse,
                    text = currentContent.text
                )
                repo.saveFavorite(fav)
                _uiState.update { it.copy(userMessage = "Guardado en favoritos") }
            }
        }
    }

    fun removeFavorite(id: String) {
        favoritesRepository?.let { repo ->
            viewModelScope.launch {
                repo.removeFavorite(id)
            }
        }
    }

    fun selectFavoriteVerse(fav: FavoriteVerse) {
        // Encontrar el libro correspondiente en la lista de libros si coincide la traducción
        val matchedBook = _uiState.value.books.firstOrNull { it.bookId == fav.bookId } ?: BibleBook(
            bookId = fav.bookId,
            name = fav.bookName,
            chapters = 150
        )
        _uiState.update {
            it.copy(
                selectedTranslation = fav.translation,
                selectedBook = matchedBook,
                availableChapters = (1..matchedBook.chapters).toList(),
                selectedChapter = fav.chapter,
                availableVerses = (1..150).toList(),
                selectedVerse = fav.verse,
                currentVerseContent = com.example.blesearchum.model.BibleVerse(
                    pk = 0,
                    verse = fav.verse,
                    text = fav.text
                ),
                isCurrentVerseFavorite = true,
                showFavoritesSheet = false
            )
        }
    }

    fun setShowFavoritesSheet(show: Boolean) {
        _uiState.update { it.copy(showFavoritesSheet = show) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
