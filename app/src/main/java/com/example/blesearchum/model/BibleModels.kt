package com.example.blesearchum.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un libro de la Biblia retornado por el API de Bolls.
 */
data class BibleBook(
    @SerializedName("bookid") val bookId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("chapters") val chapters: Int
)

/**
 * Representa el contenido de un versículo individual retornado por el API.
 */
data class BibleVerse(
    @SerializedName("pk") val pk: Int,
    @SerializedName("verse") val verse: Int,
    @SerializedName("text") val text: String
)

/**
 * Representa una traducción bíblica disponible (Reto 3).
 */
data class BibleTranslation(
    val code: String,
    val name: String
)

/**
 * Modelo de datos para guardar versículos en favoritos (Reto 2).
 */
data class FavoriteVerse(
    val id: String, // e.g., "RV1960-1-1-1"
    val translation: String,
    val bookName: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
