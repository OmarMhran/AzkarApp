package com.example.azkarapp.presentation.quran

import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.model.LastRead
import com.example.azkarapp.domain.model.Surah

data class QuranListUiState(
    val isPreparing: Boolean = true,
    val error: String? = null,
    val surahs: List<Surah> = emptyList(),
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<AyahMatch> = emptyList(),
    val showBookmarks: Boolean = false,
    val bookmarks: List<AyahMatch> = emptyList(),
    val lastRead: LastRead? = null
)
