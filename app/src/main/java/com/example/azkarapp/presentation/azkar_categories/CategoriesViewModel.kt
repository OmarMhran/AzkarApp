package com.example.azkarapp.presentation.azkar_categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sectionId: Int = checkNotNull(savedStateHandle["sectionId"])

    private val _uiState = MutableStateFlow(
        CategoriesUiState(sectionName = checkNotNull(savedStateHandle["sectionName"]))
    )
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun onAction(action: CategoriesAction) {
        when (action) {
            CategoriesAction.Retry -> loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = azkarRepository.getCategoriesByGroup(sectionId)
                _uiState.update { it.copy(isLoading = false, categories = list) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "حدث خطأ ما") }
            }
        }
    }
}
