package com.example.azkarapp.presentation.hafz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.model.HafzProgress
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.PageBlock
import com.example.azkarapp.domain.recitation.ExpectedWord
import com.example.azkarapp.domain.recitation.RecitationMatcher
import com.example.azkarapp.domain.recitation.RecitationResult
import com.example.azkarapp.domain.recitation.SpeechEvent
import com.example.azkarapp.domain.recitation.SpeechFailure
import com.example.azkarapp.domain.recitation.SpeechSource
import com.example.azkarapp.domain.usecase.hafz.GetLastHafzPageUseCase
import com.example.azkarapp.domain.usecase.hafz.ObserveHafzProgressUseCase
import com.example.azkarapp.domain.usecase.hafz.SaveLastHafzPageUseCase
import com.example.azkarapp.domain.usecase.hafz.SetPageMemorizedUseCase
import com.example.azkarapp.domain.usecase.quran.EnsureQuranReadyUseCase
import com.example.azkarapp.domain.usecase.quran.GetMushafPageUseCase
import com.example.azkarapp.domain.usecase.quran.SearchQuranUseCase
import com.example.azkarapp.domain.util.ArabicNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** محتوى صفحة جاهز للعرض. */
data class PageContent(
    val words: List<ExpectedWord>,
    /** فهرس الكلمة اللي بتنتهي بيها الآية -> رقم الآية. */
    val ayahEnds: Map<Int, Int>,
    val surahName: String
)

data class SearchHit(val title: String, val subtitle: String, val page: Int, val isAyah: Boolean)

data class HafzUiState(
    val isReady: Boolean = false,
    val error: String? = null,
    val initialPage: Int = 1,
    val currentPage: Int = 1,
    val pages: Map<Int, PageContent> = emptyMap(),
    val failedPages: Set<Int> = emptySet(),
    val progress: HafzProgress = HafzProgress(),
    /** نتيجة التسميع للصفحة الحالية بس. */
    val result: RecitationResult = RecitationResult.empty(0),
    val isListening: Boolean = false,
    val showText: Boolean = false,
    val message: String? = null,
    val showPassedDialog: Boolean = false,
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val searchHits: List<SearchHit> = emptyList()
) {
    val isMemorized: Boolean get() = currentPage in progress.memorizedPages
}

sealed interface HafzAction {
    data class EnsurePage(val page: Int) : HafzAction
    data class PageChanged(val page: Int) : HafzAction
    data object Start : HafzAction
    data object Stop : HafzAction
    data object Reset : HafzAction
    data object ToggleShowText : HafzAction
    data object PermissionDenied : HafzAction
    data object DismissPassed : HafzAction
    data class SetMemorized(val memorized: Boolean) : HafzAction
    data object OpenSearch : HafzAction
    data object CloseSearch : HafzAction
    data class SearchChanged(val query: String) : HafzAction
    data class JumpTo(val page: Int) : HafzAction
    data object Retry : HafzAction
}

@HiltViewModel
class HafzViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ensureQuranReady: EnsureQuranReadyUseCase,
    private val getMushafPage: GetMushafPageUseCase,
    private val searchQuran: SearchQuranUseCase,
    private val speechSource: SpeechSource,
    private val setPageMemorized: SetPageMemorizedUseCase,
    private val getLastPage: GetLastHafzPageUseCase,
    private val saveLastPage: SaveLastHafzPageUseCase,
    observeProgress: ObserveHafzProgressUseCase
) : ViewModel() {

    private val requestedPage: Int = savedStateHandle.get<Int>("page") ?: -1

    private val _state = MutableStateFlow(HafzUiState())
    val uiState: StateFlow<HafzUiState> = _state.asStateFlow()

    /** أوامر التنقل لشاشة الـ Pager (من البحث). */
    private val _jumps = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val jumps: SharedFlow<Int> = _jumps.asSharedFlow()

    private var listenJob: Job? = null
    private var searchJob: Job? = null
    private val loading = HashSet<Int>()

    /** نص الجلسات اللي خلصت (كل جلسة استماع بتخلص بعد سكوت، فبنكمّل فوقها). */
    private var committed = ""

    init {
        viewModelScope.launch {
            observeProgress().collect { progress -> _state.update { it.copy(progress = progress) } }
        }
        prepare()
    }

    fun onAction(action: HafzAction) {
        when (action) {
            is HafzAction.EnsurePage -> ensurePage(action.page)
            is HafzAction.PageChanged -> pageChanged(action.page)
            HafzAction.Start -> start()
            HafzAction.Stop -> stop()
            HafzAction.Reset -> resetRecitation()
            HafzAction.ToggleShowText -> _state.update { it.copy(showText = !it.showText) }
            HafzAction.PermissionDenied -> _state.update {
                it.copy(message = "محتاج صلاحية الميكروفون علشان أسمع تلاوتك.")
            }
            HafzAction.DismissPassed -> _state.update { it.copy(showPassedDialog = false) }
            is HafzAction.SetMemorized -> {
                val page = _state.value.currentPage
                viewModelScope.launch { setPageMemorized(page, action.memorized) }
            }
            HafzAction.OpenSearch -> _state.update { it.copy(searchOpen = true) }
            HafzAction.CloseSearch -> {
                searchJob?.cancel()
                _state.update { it.copy(searchOpen = false, searchQuery = "", searchHits = emptyList()) }
            }
            is HafzAction.SearchChanged -> search(action.query)
            is HafzAction.JumpTo -> {
                onAction(HafzAction.CloseSearch)
                _jumps.tryEmit(action.page.coerceIn(1, MushafPage.TOTAL_PAGES))
            }
            HafzAction.Retry -> prepare()
        }
    }

    // ───────────── تجهيز وتحميل الصفحات ─────────────

    private fun prepare() {
        _state.update { it.copy(error = null) }
        viewModelScope.launch {
            try {
                ensureQuranReady()
                val start = (requestedPage.takeIf { it in 1..MushafPage.TOTAL_PAGES }
                    ?: getLastPage() ?: 1)
                _state.update { it.copy(isReady = true, initialPage = start, currentPage = start) }
                ensurePage(start)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(error = "تعذر تجهيز المصحف") }
            }
        }
    }

    private fun ensurePage(page: Int) {
        if (page !in 1..MushafPage.TOTAL_PAGES) return
        val st = _state.value
        if (st.pages.containsKey(page) || !loading.add(page)) return
        viewModelScope.launch {
            try {
                val mushaf = getMushafPage(page)
                val ayahs = mushaf.blocks.filterIsInstance<PageBlock.Verses>().flatMap { it.ayahs }
                val content = withContext(Dispatchers.Default) {
                    val words = RecitationMatcher.buildWords(ayahs)
                    val ends = HashMap<Int, Int>()
                    words.forEachIndexed { index, word ->
                        val next = words.getOrNull(index + 1)
                        if (next == null || next.ayahNumber != word.ayahNumber ||
                            next.surahNumber != word.surahNumber
                        ) ends[index] = word.ayahNumber
                    }
                    PageContent(words, ends, mushaf.surahName)
                }
                _state.update { s ->
                    s.copy(
                        pages = s.pages + (page to content),
                        failedPages = s.failedPages - page,
                        result = if (s.currentPage == page && s.result.total != content.words.size)
                            RecitationResult.empty(content.words.size) else s.result
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(failedPages = it.failedPages + page) }
            } finally {
                loading.remove(page)
            }
        }
    }

    private fun pageChanged(page: Int) {
        if (page !in 1..MushafPage.TOTAL_PAGES) return
        if (page != _state.value.currentPage) {
            stop()
            committed = ""
            _state.update {
                it.copy(
                    currentPage = page,
                    result = RecitationResult.empty(it.pages[page]?.words?.size ?: 0),
                    message = null,
                    showPassedDialog = false
                )
            }
            viewModelScope.launch { saveLastPage(page) }
        }
        ensurePage(page)
    }

    // ───────────── البحث ─────────────

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            val hits = withContext(Dispatchers.Default) { buildHits(query) }
            _state.update { it.copy(searchHits = hits) }
        }
    }

    private suspend fun buildHits(query: String): List<SearchHit> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val digits = trimmed.map { if (it in '٠'..'٩') '0' + (it - '٠') else it }.joinToString("")
        digits.toIntOrNull()?.let { number ->
            return if (number in 1..MushafPage.TOTAL_PAGES) {
                listOf(SearchHit("صفحة $number", "", number, isAyah = false))
            } else emptyList()
        }

        val normalized = ArabicNormalizer.normalize(trimmed)
        if (normalized.isEmpty()) return emptyList()

        val surahHits = _state.value.progress.surahs
            .filter { ArabicNormalizer.normalize(it.surah.name).contains(normalized) }
            .map { SearchHit(it.surah.name, "صفحة ${it.firstPage}", it.firstPage, isAyah = false) }

        val ayahHits = searchQuran(trimmed).take(MAX_AYAH_HITS).map {
            SearchHit(
                title = it.text,
                subtitle = "${it.surahName} • آية ${it.key.ayahNumber} • صفحة ${it.page}",
                page = it.page,
                isAyah = true
            )
        }
        return surahHits + ayahHits
    }

    // ───────────── التسميع ─────────────

    private fun resetRecitation() {
        stop()
        committed = ""
        _state.update {
            it.copy(
                result = RecitationResult.empty(it.pages[it.currentPage]?.words?.size ?: 0),
                message = null
            )
        }
    }

    private fun start() {
        val st = _state.value
        val words = st.pages[st.currentPage]?.words.orEmpty()
        if (listenJob?.isActive == true || words.isEmpty()) return
        if (!speechSource.isAvailable()) {
            _state.update {
                it.copy(message = "جهازك مفيهوش خدمة تعرّف على الصوت. ثبّت أو فعّل Google وجرّب تاني.")
            }
            return
        }
        _state.update { it.copy(isListening = true, message = null) }
        val page = st.currentPage
        listenJob = viewModelScope.launch { listenLoop(page, words) }
    }

    private fun stop() {
        listenJob?.cancel()
        listenJob = null
        _state.update { it.copy(isListening = false) }
    }

    private suspend fun listenLoop(page: Int, words: List<ExpectedWord>) {
        var retries = 0
        while (currentCoroutineContext().isActive) {
            var failure: SpeechFailure? = null
            var gotText = false
            try {
                speechSource.listen().collect { event ->
                    when (event) {
                        SpeechEvent.Ready -> Unit
                        is SpeechEvent.Partial -> {
                            gotText = true
                            publish(words, join(committed, event.text))
                            if (_state.value.result.isPass) throw PassReached
                        }
                        is SpeechEvent.Final -> {
                            if (event.text.isNotBlank()) {
                                gotText = true
                                committed = join(committed, event.text)
                                publish(words, committed)
                                if (_state.value.result.isPass) throw PassReached
                            }
                        }
                        is SpeechEvent.Failure -> failure = event.reason
                    }
                }
            } catch (e: PassReached) {
                // نجح التسميع: بنقفل الاستماع فوراً من غير ما نستنى السكوت
            }

            val result = _state.value.result
            if (result.isPass) {
                setPageMemorized(page, true) // idempotent
                _state.update { it.copy(isListening = false, showPassedDialog = true) }
                listenJob = null
                return
            }
            if (result.reachedEnd) {
                stopWithMessage(
                    "وصلت لآخر الصفحة، بس فيه ${result.wrong} كلمة غلط أو فايتة. راجع الكلمات الحمرا وجرّب تاني."
                )
                return
            }

            when (failure) {
                null, SpeechFailure.NO_SPEECH -> {
                    retries = 0
                    if (!gotText) delay(250) // جلسة فاضية: منزوّدش الضغط على المحرك
                }
                SpeechFailure.RETRYABLE -> {
                    if (++retries > MAX_RETRIES) {
                        stopWithMessage("المحرك الصوتي مش مستجيب. جرّب تاني بعد شوية.")
                        return
                    }
                    delay(400)
                }
                SpeechFailure.NETWORK, SpeechFailure.UNAVAILABLE -> {
                    stopWithMessage("اللغة العربية مش متحمّلة على جهازك للتعرف على الصوت. افتح النت وجرّب تاني.")
                    return
                }
                SpeechFailure.NO_PERMISSION -> {
                    stopWithMessage("محتاج صلاحية الميكروفون علشان أسمع تلاوتك.")
                    return
                }
            }
        }
    }

    private suspend fun publish(words: List<ExpectedWord>, text: String) {
        val result = withContext(Dispatchers.Default) { RecitationMatcher.match(words, text) }
        _state.update { it.copy(result = result) }
    }

    private fun stopWithMessage(message: String) {
        _state.update { it.copy(isListening = false, message = message) }
        listenJob = null
    }

    private fun join(a: String, b: String) = if (a.isBlank()) b else "$a $b"

    private object PassReached : RuntimeException()

    private companion object {
        const val MAX_RETRIES = 3
        const val MAX_AYAH_HITS = 30
    }
}