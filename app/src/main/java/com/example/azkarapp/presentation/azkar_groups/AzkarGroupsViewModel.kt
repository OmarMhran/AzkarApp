package com.example.azkarapp.presentation.azkar_groups

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
class AzkarGroupsViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AzkarGroupsUiState())
    val uiState: StateFlow<AzkarGroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun onAction(action: AzkarGroupsAction) {
        when (action) {
            AzkarGroupsAction.Retry -> loadGroups()
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = azkarRepository.getGroups()
                _uiState.update { it.copy(isLoading = false, groups = groups) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "حدث خطأ ما")
                }
            }
        }
    }
}
