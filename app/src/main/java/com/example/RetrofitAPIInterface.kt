package com.example

import retrofit2.http.GET
import retrofit2.http.Query


interface MangaApi {
    @GET("top/manga")
    suspend fun getTopManga(
        @Query("page") page: Int = 1
    ): MangaResponse
}

