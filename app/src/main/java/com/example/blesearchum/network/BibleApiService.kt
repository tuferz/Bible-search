package com.example.blesearchum.network

import com.example.blesearchum.model.BibleBook
import com.example.blesearchum.model.BibleVerse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface BibleApiService {

    @GET("get-books/RV1960/")
    suspend fun getBooks(): List<BibleBook>

    @GET("get-verse/RV1960/{bookId}/{chapter}/{verse}/")
    suspend fun getVerse(
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int,
        @Path("verse") verse: Int
    ): BibleVerse

    // Endpoints dinámicos para soporte multi-versión (Reto Adicional 3)
    @GET("get-books/{translation}/")
    suspend fun getBooksByTranslation(
        @Path("translation") translation: String
    ): List<BibleBook>

    @GET("get-verse/{translation}/{bookId}/{chapter}/{verse}/")
    suspend fun getVerseByTranslation(
        @Path("translation") translation: String,
        @Path("bookId") bookId: Int,
        @Path("chapter") chapter: Int,
        @Path("verse") verse: Int
    ): BibleVerse
}

object RetrofitInstance {
    private const val BASE_URL = "https://bolls.life/"

    // Interceptor para normalizar 'RVR1960' a 'RV1960' si se usa la nomenclatura alterna
    private val translationNormalizerInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()
        val fixedUrl = if (originalUrl.contains("/RVR1960/")) {
            originalUrl.replace("/RVR1960/", "/RV1960/")
        } else {
            originalUrl
        }
        val newRequest = originalRequest.newBuilder()
            .url(fixedUrl)
            .header("User-Agent", "BibleSearchUM-Android")
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(translationNormalizerInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: BibleApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BibleApiService::class.java)
    }
}
