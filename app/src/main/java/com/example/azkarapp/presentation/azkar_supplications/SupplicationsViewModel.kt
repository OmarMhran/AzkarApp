package com.example.azkarapp.presentation.azkar_supplications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.AzkarProgressRepository
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.domain.model.AzkarSupplication
import com.example.azkarapp.domain.repository.AzkarRepository
import com.example.azkarapp.util.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplicationsViewModel @Inject constructor(
    private val azkarRepository: AzkarRepository,
    private val settingsRepository: SettingsRepository,
    private val vibrationHelper: VibrationHelper,
    private val azkarProgressRepository: AzkarProgressRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Int = checkNotNull(savedStateHandle["categoryId"])
    private val categoryName: String = checkNotNull(savedStateHandle["categoryName"])

    @Volatile
    private var isVibrationActive = true

    private val _uiState = MutableStateFlow(SupplicationsUiState(categoryName = categoryName))
    val uiState: StateFlow<SupplicationsUiState> = _uiState.asStateFlow()

    init {
        loadSupplications()
        viewModelScope.launch {
            settingsRepository.isVibrationEnabled.collect { isVibrationActive = it }
        }
        viewModelScope.launch {
            settingsRepository.fontSize.collect { size ->
                _uiState.update { it.copy(fontSize = size) }
            }
        }
    }

    fun onAction(action: SupplicationsAction) {
        when (action) {
            is SupplicationsAction.Tap -> tap(action.supplicationId, action.onFinished)
            SupplicationsAction.DismissCompletionDialog ->
                _uiState.update { it.copy(showCompletionDialog = false) }
            SupplicationsAction.Retry -> loadSupplications()
        }
    }

    private fun loadSupplications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = azkarRepository.getSupplicationsByCategory(categoryId)
                val saved = azkarProgressRepository.progress.first()
                    ?.takeIf { it.categoryId == categoryId }

                val (counts, firstIncomplete) = restoreCounts(list, completedCount = saved?.completed ?: 0)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        supplications = list,
                        currentCounts = counts,
                        initialIndex = firstIncomplete
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "حدث خطأ غير متوقع") }
            }
        }
    }

    /** أول [completedCount] ذكر بيتحسبوا خلصانين، والباقي بتكراره الكامل. */
    private fun restoreCounts(
        list: List<AzkarSupplication>,
        completedCount: Int
    ): Pair<Map<Int, Int>, Int> {
        val counts = LinkedHashMap<Int, Int>(list.size)
        var firstIncomplete = 0
        list.forEachIndexed { index, item ->
            if (index < completedCount) {
                counts[item.id] = 0
            } else {
                counts[item.id] = item.repeat
                if (index == completedCount) firstIncomplete = index
            }
        }
        return counts to firstIncomplete
    }

    private fun tap(supplicationId: Int, onFinished: () -> Unit) {
        val current = _uiState.value
        val count = current.currentCounts[supplicationId] ?: 0
        if (count <= 0) return

        vibrationHelper.vibrateTick(isVibrationActive)

        val newCount = count - 1
        val counts = current.currentCounts + (supplicationId to newCount)
        val allFinished = counts.isNotEmpty() && counts.values.all { it == 0 }

        _uiState.update { it.copy(currentCounts = counts, showCompletionDialog = allFinished) }

        if (newCount == 0) {
            vibrationHelper.vibrateCompletion(isVibrationActive)
            onFinished()
        }

        val completed = counts.values.count { it == 0 }
        val total = counts.size
        viewModelScope.launch {
            if (allFinished) {
                azkarProgressRepository.clearProgress(categoryId)
            } else {
                azkarProgressRepository.saveProgress(categoryId, categoryName, completed, total)
            }
        }
    }
}
