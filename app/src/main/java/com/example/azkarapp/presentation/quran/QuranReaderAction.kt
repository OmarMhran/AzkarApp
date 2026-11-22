package com.example.azkarapp.presentation.quran

sealed interface QuranReaderAction {
    data class PageRequested(val page: Int) : QuranReaderAction
    data class PageSettled(val page: Int) : QuranReaderAction
    data object ClearHighlight : QuranReaderAction
    data class AyahSelected(val selected: SelectedAyah) : QuranReaderAction
    data object DismissAyahMenu : QuranReaderAction
    data object ToggleSelectedBookmark : QuranReaderAction
    data class RetryPage(val page: Int) : QuranReaderAction
}
