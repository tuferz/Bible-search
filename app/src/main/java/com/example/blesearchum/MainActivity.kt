package com.example.blesearchum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blesearchum.data.FavoritesRepository
import com.example.blesearchum.ui.BibleSearchScreen
import com.example.blesearchum.ui.BibleViewModel
import com.example.blesearchum.ui.theme.BibleSearchUMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val favoritesRepository = FavoritesRepository(applicationContext)

        setContent {
            BibleSearchUMTheme {
                val viewModel: BibleViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return BibleViewModel(favoritesRepository = favoritesRepository) as T
                        }
                    }
                )
                BibleSearchScreen(viewModel = viewModel)
            }
        }
    }
}
