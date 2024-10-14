package com.example

import retrofit2.http.GET

// MangaApi.kt
interface MangaApi {
    @GET("top/manga")
    suspend fun getTopManga(): MangaResponse
}
