package com.example.azkarapp.presentation.quran

import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.model.MushafPage

data class SelectedAyah(
    val key: AyahKey,
    val surahName: String,
    val text: String
)

data class QuranReaderUiState(

    val initialPage: Int = 1,
    val pages: Map<Int, MushafPage> = emptyMap(),
    val failedPages: Set<Int> = emptySet(),
    val bookmarked: Set<AyahKey> = emptySet(),
    val fontSize: Float = 22f,
    val highlighted: AyahKey? = null,
    val selectedAyah: SelectedAyah? = null,
    val isSelectedBookmarked: Boolean = false
)
