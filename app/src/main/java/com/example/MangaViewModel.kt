package com.example

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// MangaViewModel.kt
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
}
