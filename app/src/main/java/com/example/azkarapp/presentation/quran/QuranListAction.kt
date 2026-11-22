package com.example.azkarapp.presentation.quran

sealed interface QuranListAction {
    data class QueryChanged(val query: String) : QuranListAction
    data object ToggleBookmarksView : QuranListAction
    data object Retry : QuranListAction
}
