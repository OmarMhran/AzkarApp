package com.example.azkarapp.presentation.azkar_supplications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.local.AzkarProgressDataSource
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.domain.repository.AzkarRepository
import com.example.azkarapp.util.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SupplicationsViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository,
    private val settingsRepository: SettingsRepository,
    private val vibrationHelper: VibrationHelper,
    private val azkarProgressDataSource: AzkarProgressDataSource,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val categoryId: Int = checkNotNull(savedStateHandle["categoryId"])
    val categoryName: String = checkNotNull(savedStateHandle["categoryName"])

    private var isVibrationActive: Boolean = true

    private val _state = MutableStateFlow(SupplicationState())
    val state: StateFlow<SupplicationState> = _state.asStateFlow()

    val fontSize: StateFlow<Float> = settingsRepository.fontSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 18f
    )


    init {
        loadSupplications()
        viewModelScope.launch {
            settingsRepository.isVibrationEnabled.collect { enabled ->
                isVibrationActive = enabled
            }
        }
    }

    private fun loadSupplications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = azkarRepository.getSupplicationsByCategory(categoryId)
                val savedProgress = azkarProgressDataSource.progressFlow.first()

                val initialCounts = mutableMapOf<Int, Int>()
                var firstIncompleteIdx = 0
                var foundFirstIncomplete = false

                if (savedProgress != null && savedProgress.categoryId == categoryId) {
                    var completedLeft = savedProgress.completed
                    list.forEachIndexed { index, item ->
                        if (completedLeft > 0) {
                            initialCounts[item.id] = 0
                            completedLeft--
                        } else {
                            initialCounts[item.id] = item.repeat
                            if (!foundFirstIncomplete) {
                                firstIncompleteIdx = index
                                foundFirstIncomplete = true
                            }
                        }
                    }
                } else {
                    list.forEach { item ->
                        initialCounts[item.id] = item.repeat
                    }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        supplications = list,
                        currentCounts = initialCounts,
                        initialIndex = firstIncompleteIdx
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun decrementCount(supplicationId: Int, onFinished: () -> Unit = {}) {
        var updatedCounts: Map<Int, Int>? = null
        var allFinished = false

        _state.update { currentState ->
            val currentCount = currentState.currentCounts[supplicationId] ?: 0
            if (currentCount > 0) {
                val newCount = currentCount - 1
                if (newCount == 0) {
                    onFinished()
                }
                val counts = currentState.currentCounts.toMutableMap().apply {
                    put(supplicationId, newCount)
                }
                allFinished = counts.isNotEmpty() && counts.values.all { it == 0 }
                updatedCounts = counts

                currentState.copy(
                    currentCounts = counts,
                    showCompletionDialog = allFinished
                )
            } else {
                currentState
            }
        }

        val counts = updatedCounts ?: return
        val completed = counts.values.count { it == 0 }
        val total = counts.size

        viewModelScope.launch(Dispatchers.IO) {
            if (allFinished) {
                azkarProgressDataSource.clearProgress(categoryId)
            } else {
                azkarProgressDataSource.saveProgress(categoryId, categoryName, completed, total)
            }
        }
    }

    fun dismissCompletionDialog() {
        _state.update { it.copy(showCompletionDialog = false) }
    }

    fun triggerTick() {
        vibrationHelper.vibrateTick(isVibrationActive)
    }

    fun triggerCompletion() {
        vibrationHelper.vibrateCompletion(isVibrationActive)
    }
}