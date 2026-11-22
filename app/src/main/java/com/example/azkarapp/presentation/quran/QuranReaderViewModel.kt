package com.example.azkarapp.presentation.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.usecase.quran.GetMushafPageUseCase
import com.example.azkarapp.domain.usecase.quran.ObserveBookmarkedKeysUseCase
import com.example.azkarapp.domain.usecase.quran.SaveLastReadUseCase
import com.example.azkarapp.domain.usecase.quran.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuranReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMushafPage: GetMushafPageUseCase,
    observeBookmarkedKeys: ObserveBookmarkedKeysUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val saveLastRead: SaveLastReadUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val initialPage: Int =
        (savedStateHandle.get<Int>("page") ?: 1).coerceIn(1, MushafPage.TOTAL_PAGES)

    private val pages = MutableStateFlow<Map<Int, MushafPage>>(emptyMap())
    private val failedPages = MutableStateFlow<Set<Int>>(emptySet())
    private val selected = MutableStateFlow<SelectedAyah?>(null)
    private val highlighted = MutableStateFlow(
        run {
            val surah = savedStateHandle.get<Int>("surah") ?: -1
            val ayah = savedStateHandle.get<Int>("ayah") ?: -1
            if (surah > 0 && ayah > 0) AyahKey(surah, ayah) else null
        }
    )
    private val loading = mutableSetOf<Int>()

    private data class Transient(
        val pages: Map<Int, MushafPage>,
        val failed: Set<Int>,
        val selected: SelectedAyah?,
        val highlighted: AyahKey?
    )

    private val pagesState = combine(pages, failedPages, selected, highlighted) { p, f, s, h ->
        Transient(p, f, s, h)
    }

    val uiState: StateFlow<QuranReaderUiState> = combine(
        pagesState,
        observeBookmarkedKeys(),
        settingsRepository.fontSize
    ) { transient, bookmarked, fontSize ->
        val selectedAyah = transient.selected
        QuranReaderUiState(
            initialPage = initialPage,
            pages = transient.pages,
            failedPages = transient.failed,
            highlighted = transient.highlighted,
            bookmarked = bookmarked,
            fontSize = fontSize,
            selectedAyah = selectedAyah,
            isSelectedBookmarked = selectedAyah != null && selectedAyah.key in bookmarked
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        QuranReaderUiState(initialPage = initialPage)
    )

    fun onAction(action: QuranReaderAction) {
        when (action) {
            is QuranReaderAction.PageRequested -> loadPage(action.page)
            is QuranReaderAction.RetryPage -> {
                failedPages.update { it - action.page }
                loadPage(action.page)
            }

            is QuranReaderAction.PageSettled -> viewModelScope.launch { saveLastRead(action.page) }
            QuranReaderAction.ClearHighlight -> highlighted.value = null
            is QuranReaderAction.AyahSelected -> {
                highlighted.value = null
                selected.value = action.selected
            }
            QuranReaderAction.DismissAyahMenu -> selected.value = null
            QuranReaderAction.ToggleSelectedBookmark -> {
                val key = selected.value?.key ?: return
                selected.value = null
                viewModelScope.launch { toggleBookmark(key) }
            }
        }
    }

    private fun loadPage(page: Int) {
        if (page !in 1..MushafPage.TOTAL_PAGES) return
        if (page in pages.value || !loading.add(page)) return
        viewModelScope.launch {
            try {
                val loaded = getMushafPage(page)
                pages.update { it + (page to loaded) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failedPages.update { it + page }
            } finally {
                loading.remove(page)
            }
        }
    }
}
