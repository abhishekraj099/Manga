package com.example

import javax.inject.Inject

// GetTopMangaUseCase.kt
class GetTopMangaUseCase @Inject constructor(
    private val repository: MangaRepository
) {
    suspend operator fun invoke(): List<Manga> {
        return repository.getTopManga()
    }
}
