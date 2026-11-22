package com.example.azkarapp.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.model.AyahMatch
import com.example.azkarapp.domain.model.LastRead
import com.example.azkarapp.domain.model.QuranDataException
import com.example.azkarapp.domain.model.Surah
import com.example.azkarapp.domain.usecase.quran.EnsureQuranReadyUseCase
import com.example.azkarapp.domain.usecase.quran.ObserveBookmarksUseCase
import com.example.azkarapp.domain.usecase.quran.ObserveLastReadUseCase
import com.example.azkarapp.domain.usecase.quran.ObserveSurahsUseCase
import com.example.azkarapp.domain.usecase.quran.SearchQuranUseCase
import com.example.azkarapp.domain.util.ArabicNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class QuranListViewModel @Inject constructor(
    private val ensureQuranReady: EnsureQuranReadyUseCase,
    observeSurahs: ObserveSurahsUseCase,
    private val searchQuran: SearchQuranUseCase,
    observeBookmarks: ObserveBookmarksUseCase,
    observeLastRead: ObserveLastReadUseCase
) : ViewModel() {

    private sealed interface Prep {
        data object Loading : Prep
        data object Ready : Prep
        data class Failed(val message: String) : Prep
    }

    private data class Content(
        val surahs: List<Surah> = emptyList(),
        val results: List<AyahMatch> = emptyList(),
        val bookmarks: List<AyahMatch> = emptyList(),
        val lastRead: LastRead? = null
    )

    private val prep = MutableStateFlow<Prep>(Prep.Loading)
    private val query = MutableStateFlow("")
    private val showBookmarks = MutableStateFlow(false)

    private val searchResults: Flow<List<AyahMatch>> = query
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .mapLatest { searchQuran(it) }

    private val content: Flow<Content> = prep.flatMapLatest { state ->
        if (state !is Prep.Ready) {
            flowOf(Content())
        } else {
            combine(
                observeSurahs(),
                searchResults,
                observeBookmarks(),
                observeLastRead()
            ) { surahs, results, bookmarks, lastRead ->
                Content(surahs, results, bookmarks, lastRead)
            }
        }
    }

    val uiState: StateFlow<QuranListUiState> = combine(
        prep, query, showBookmarks, content
    ) { prepState, q, bookmarksView, c ->
        QuranListUiState(
            isPreparing = prepState is Prep.Loading,
            error = (prepState as? Prep.Failed)?.message,
            surahs = c.surahs,
            query = q,
            isSearching = ArabicNormalizer.normalize(q).length >= SearchQuranUseCase.MIN_QUERY_LENGTH,
            results = c.results,
            showBookmarks = bookmarksView,
            bookmarks = c.bookmarks,
            lastRead = c.lastRead
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QuranListUiState())

    init {
        prepare()
    }

    fun onAction(action: QuranListAction) {
        when (action) {
            is QuranListAction.QueryChanged -> query.value = action.query
            QuranListAction.ToggleBookmarksView -> showBookmarks.value = !showBookmarks.value
            QuranListAction.Retry -> prepare()
        }
    }

    private fun prepare() {
        prep.value = Prep.Loading
        viewModelScope.launch {
            prep.value = try {
                ensureQuranReady()
                Prep.Ready
            } catch (e: CancellationException) {
                throw e
            } catch (e: QuranDataException) {
                Prep.Failed(e.message ?: "ملف المصحف غير صالح")
            } catch (e: Exception) {
                Prep.Failed("تعذر تجهيز المصحف")
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
