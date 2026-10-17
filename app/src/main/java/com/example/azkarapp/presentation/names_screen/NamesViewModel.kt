package com.example.azkarapp.presentation.names_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.model.Names
import com.example.azkarapp.domain.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DIACRITICS = Regex("[\u064B-\u065F\u0670]")
private val ALEF_VARIANTS = Regex("[أإآ]")

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

    private val _state = MutableStateFlow(NamesState())
    val state: StateFlow<NamesState> = _state.asStateFlow()

    private var searchIndex: List<Pair<Names, String>> = emptyList()

    init {
        loadNames()
    }

    private fun loadNames() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val fetchedNames = azkarRepository.getNames()
                searchIndex = fetchedNames.map { it to it.name.normalizeArabic() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        names = fetchedNames,
                        originalNames = fetchedNames
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "حدث خطأ ما")
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val cleaned = query.trim().normalizeArabic()
        val filtered = if (cleaned.isEmpty()) {
            _state.value.originalNames
        } else {
            searchIndex.filter { it.second.contains(cleaned) }.map { it.first }
        }
        _state.update { it.copy(searchQuery = query, names = filtered) }
    }


    fun selectName(name: Names?) {
        _state.update { it.copy(selectedName = name) }
    }
}