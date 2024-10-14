package com.example

import javax.inject.Inject

// MangaRepositoryImpl.kt
class MangaRepositoryImpl @Inject constructor(
    private val api: MangaApi
) : MangaRepository {
    override suspend fun getTopManga(): List<Manga> {
        val response = api.getTopManga()
        // Map network model (MangaData) to domain model (Manga)
        return response.data.map { mangaData ->
            Manga(
                mal_id = mangaData.mal_id,
                url = mangaData.url,
                title = mangaData.title,
                synopsis = mangaData.synopsis,
                score = mangaData.score,
                members = mangaData.members,
                images = mangaData.images, // This now refers to MangaImages
                genres = mangaData.genres,
                authors = mangaData.authors
            )
        }
    }
}
