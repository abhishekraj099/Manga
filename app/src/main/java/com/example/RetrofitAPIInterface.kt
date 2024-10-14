package com.example

import retrofit2.http.GET


interface MangaApi {
    @GET("top/manga")
    suspend fun getTopManga(): MangaResponse
}
