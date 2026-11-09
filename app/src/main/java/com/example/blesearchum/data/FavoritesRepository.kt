package com.example.blesearchum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.blesearchum.model.FavoriteVerse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bible_favorites")

class FavoritesRepository(private val context: Context) {
    private val gson = Gson()
    private val favoritesKey = stringPreferencesKey("favorites_json")

    val favoritesFlow: Flow<List<FavoriteVerse>> = context.dataStore.data.map { preferences ->
        val json = preferences[favoritesKey] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<FavoriteVerse>>() {}.type
            gson.fromJson<List<FavoriteVerse>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveFavorite(favorite: FavoriteVerse) {
        context.dataStore.edit { preferences ->
            val json = preferences[favoritesKey]
            val currentList: MutableList<FavoriteVerse> = if (json != null) {
                try {
                    val type = object : TypeToken<List<FavoriteVerse>>() {}.type
                    gson.fromJson<List<FavoriteVerse>>(json, type)?.toMutableList() ?: mutableListOf()
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            currentList.removeAll { it.id == favorite.id }
            currentList.add(0, favorite)
            preferences[favoritesKey] = gson.toJson(currentList)
        }
    }

    suspend fun removeFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[favoritesKey] ?: return@edit
            try {
                val type = object : TypeToken<List<FavoriteVerse>>() {}.type
                val currentList: MutableList<FavoriteVerse> = gson.fromJson<List<FavoriteVerse>>(json, type)?.toMutableList() ?: return@edit
                currentList.removeAll { it.id == id }
                preferences[favoritesKey] = gson.toJson(currentList)
            } catch (_: Exception) {
            }
        }
    }
}
