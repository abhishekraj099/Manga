package com.example

// MangaRepository.kt
interface MangaRepository {
    suspend fun getTopManga(): List<Manga>
}
