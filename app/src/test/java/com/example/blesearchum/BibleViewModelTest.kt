package com.example.blesearchum

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleVerse
import com.example.blesearchum.network.BibleApiService
import com.example.blesearchum.ui.BibleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeApiService = object : BibleApiService {
        val sampleBooks = listOf(
            BibleBook(bookId = 1, name = "Génesis", chapters = 50),
            BibleBook(bookId = 2, name = "Éxodo", chapters = 40)
        )
        val sampleVerse = BibleVerse(pk = 1, verse = 1, text = "En el principio creó Dios los cielos y la tierra.")

        override suspend fun getBooks(): List<BibleBook> = sampleBooks

        override suspend fun getVerse(bookId: Int, chapter: Int, verse: Int): BibleVerse = sampleVerse

        override suspend fun getBooksByTranslation(translation: String): List<BibleBook> = sampleBooks

        override suspend fun getVerseByTranslation(
            translation: String,
            bookId: Int,
            chapter: Int,
            verse: Int
        ): BibleVerse = sampleVerse
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads books successfully`() = runTest {
        val viewModel = BibleViewModel(apiService = fakeApiService)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.books.size)
        assertEquals("Génesis", state.books[0].name)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `selecting a book populates available chapters and resets verse`() = runTest {
        val viewModel = BibleViewModel(apiService = fakeApiService)
        advanceUntilIdle()

        val book = viewModel.uiState.value.books[0] // Génesis with 50 chapters
        viewModel.onBookSelected(book)

        val state = viewModel.uiState.value
        assertEquals(book, state.selectedBook)
        assertEquals(50, state.availableChapters.size)
        assertEquals(1, state.availableChapters.first())
        assertEquals(50, state.availableChapters.last())
        assertNull(state.selectedChapter)
        assertNull(state.selectedVerse)
    }

    @Test
    fun `selecting a chapter populates 150 verses`() = runTest {
        val viewModel = BibleViewModel(apiService = fakeApiService)
        advanceUntilIdle()

        val book = viewModel.uiState.value.books[0]
        viewModel.onBookSelected(book)
        viewModel.onChapterSelected(1)

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedChapter)
        assertEquals(150, state.availableVerses.size)
        assertEquals(1, state.availableVerses.first())
        assertEquals(150, state.availableVerses.last())
        assertNull(state.selectedVerse)
    }

    @Test
    fun `selecting a verse fetches verse content successfully`() = runTest {
        val viewModel = BibleViewModel(apiService = fakeApiService)
        advanceUntilIdle()

        val book = viewModel.uiState.value.books[0]
        viewModel.onBookSelected(book)
        viewModel.onChapterSelected(1)
        viewModel.onVerseSelected(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.selectedVerse)
        assertNotNull(state.currentVerseContent)
        assertEquals(1, state.currentVerseContent?.verse)
        assertEquals("En el principio creó Dios los cielos y la tierra.", state.currentVerseContent?.text)
        assertFalse(state.isLoading)
    }

    @Test
    fun `clearSelection retains books but resets selections`() = runTest {
        val viewModel = BibleViewModel(apiService = fakeApiService)
        advanceUntilIdle()

        val book = viewModel.uiState.value.books[0]
        viewModel.onBookSelected(book)
        viewModel.onChapterSelected(1)
        viewModel.onVerseSelected(1)
        advanceUntilIdle()

        viewModel.clearSelection()

        val state = viewModel.uiState.value
        assertEquals(2, state.books.size)
        assertNull(state.selectedBook)
        assertNull(state.selectedChapter)
        assertNull(state.selectedVerse)
        assertNull(state.currentVerseContent)
    }
}
