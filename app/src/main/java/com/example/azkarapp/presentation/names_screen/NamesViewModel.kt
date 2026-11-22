package com.example.azkarapp.presentation.names_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.model.Names
import com.example.azkarapp.domain.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DIACRITICS = Regex("[ً-ٰٟ]")
private val ALEF_VARIANTS = Regex("[أإآ]")

/** بيشيل التشكيل ويوحّد الألف والتاء المربوطة والياء علشان البحث يلاقي الاسم بأي كتابة. */
private fun String.normalizeArabic(): String =
    this.replace(DIACRITICS, "")
        .replace(ALEF_VARIANTS, "ا")
        .replace('ة', 'ه')
        .replace('ى', 'ي')
        .lowercase()

@HiltViewModel
class NamesViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NamesUiState())
    val uiState: StateFlow<NamesUiState> = _uiState.asStateFlow()

    private var allNames: List<Names> = emptyList()
    private var searchIndex: List<Pair<Names, String>> = emptyList()

    init {
        loadNames()
    }

    fun onAction(action: NamesAction) {
        when (action) {
            is NamesAction.SearchChanged -> search(action.query)
            is NamesAction.SelectName -> _uiState.update { it.copy(selectedName = action.name) }
            NamesAction.DismissName -> _uiState.update { it.copy(selectedName = null) }
            NamesAction.Retry -> loadNames()
        }
    }

    private fun loadNames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val fetched = azkarRepository.getNames()
                allNames = fetched
                searchIndex = fetched.map { it to it.name.normalizeArabic() }
                _uiState.update {
                    it.copy(isLoading = false, names = filter(it.searchQuery))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "حدث خطأ ما")
                }
            }
        }
    }

    private fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query, names = filter(query)) }
    }

    private fun filter(query: String): List<Names> {
        val cleaned = query.trim().normalizeArabic()
        return if (cleaned.isEmpty()) allNames
        else searchIndex.filter { it.second.contains(cleaned) }.map { it.first }
    }
}
