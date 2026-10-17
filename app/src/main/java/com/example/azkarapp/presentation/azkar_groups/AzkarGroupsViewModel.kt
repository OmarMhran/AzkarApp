package com.example.azkarapp.presentation.azkar_groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.domain.repository.AzkarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _state = MutableStateFlow(AzkarGroupsState())
    val state: StateFlow<AzkarGroupsState> = _state.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val groups = azkarRepository.getGroups()
                _state.update { it.copy(isLoading = false, groups = groups) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.localizedMessage)
                }
            }
        }

    }
}