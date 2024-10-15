package com.example




import kotlinx.coroutines.delay

import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject



class MangaRepositoryImpl @Inject constructor(
    private val api: MangaApi
) : MangaRepository {

    private val mangaList = mutableListOf<Manga>()
    private var currentPage = 1

    override suspend fun getTopManga(): List<Manga> {
        while (mangaList.size < 100) { // Continue fetching until we have at least 100 items
            try {
                delay(1000) // Initial delay

                // Fetch the manga data for the current page
                val response: MangaResponse = api.getTopManga(page = currentPage)

                // Append new manga to the existing list
                mangaList.addAll(response.data)

                // Check if there is a next page
                if (!response.pagination.has_next_page) {
                    break // No more pages, exit the loop
                }

                // Increment the current page
                currentPage++
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    // Handle HTTP 429 Too Many Requests
                    delay(5000) // Wait for 5 seconds before retrying
                } else {
                    throw e // Rethrow other exceptions
                }
            }
        }

        // Return the mangaList (it may have more than 50 items now)
        return mangaList
    }

    fun resetPagination() {
        currentPage = 1
        mangaList.clear()
    }
}





