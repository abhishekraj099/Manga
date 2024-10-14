package com.example

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaViewModel @Inject constructor(
    private val getTopMangaUseCase: GetTopMangaUseCase
) : ViewModel() {

    private val _mangaList = mutableStateOf<List<Manga>>(emptyList())
    val mangaList: State<List<Manga>> = _mangaList

    init {
        fetchTopManga()
    }

    fun fetchTopManga() {
        viewModelScope.launch {
            try {
                val topManga = getTopMangaUseCase()
                _mangaList.value = topManga
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Function to get Manga by ID
    fun getMangaById(mangaId: Int?): Manga? {
        return _mangaList.value.find { it.id == mangaId }
    }
}

