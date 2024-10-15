package com.example

import javax.inject.Inject


class MangaRepositoryImpl @Inject constructor(
    private val api: MangaApi
) : MangaRepository {

    override suspend fun getTopManga(): List<Manga> {
        val response: MangaResponse = api.getTopManga()

        // Since `Manga` and `MangaResponse.data` structure are identical, you can directly return the list
        return response.data.map { mangaData ->
            Manga(
                mal_id = mangaData.mal_id,
                id = mangaData.id,
                url = mangaData.url,
                title = mangaData.title,
                synopsis = mangaData.synopsis,
                score = mangaData.score,
                members = mangaData.members,
                images = mangaData.images,
                genres = mangaData.genres,
                authors = mangaData.authors,
                cast = mangaData.cast // `cast` will be included if present in the API response
            )
        }
    }
}

