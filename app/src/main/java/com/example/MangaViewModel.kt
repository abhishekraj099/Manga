package com.example

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class MangaViewModel @Inject constructor(
    private val getTopMangaUseCase: GetTopMangaUseCase
) : ViewModel() {

    private val _mangaListState = MutableStateFlow<MangaListState>(MangaListState.Loading)
    val mangaListState: StateFlow<MangaListState> = _mangaListState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchTopManga()
    }

    fun fetchTopManga() {
        viewModelScope.launch {
            _mangaListState.value = MangaListState.Loading
            try {
                val topManga = getTopMangaUseCase()
                _mangaListState.value = MangaListState.Success(topManga)
            } catch (e: Exception) {
                _mangaListState.value = MangaListState.Error("Failed to load manga: ${e.message}")
            }
        }
    }

    fun refreshMangaList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchTopManga()
            _isRefreshing.value = false
        }
    }

    fun getMangaById(mangaId: Int?): Manga? {
        return when (val state = _mangaListState.value) {
            is MangaListState.Success -> state.data.find { it.id == mangaId }
            else -> null
        }
    }
}

sealed class MangaListState {
    object Loading : MangaListState()
    data class Success(val data: List<Manga>) : MangaListState()
    data class Error(val message: String) : MangaListState()
}