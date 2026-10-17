package com.example.azkarapp.presentation.main_hub.misbaha_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.MisbahaRepository
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.di.ApplicationScope
import com.example.azkarapp.util.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MisbahaViewModel @Inject constructor(
    private val repository: MisbahaRepository,
    private val settingsRepository: SettingsRepository,
    private val vibrationHelper: VibrationHelper,
    @ApplicationScope private val appScope: CoroutineScope
) : ViewModel() {

    private val selectedZikr = MutableStateFlow(DefaultAzkar.first())
    private val count = MutableStateFlow(0)

    @Volatile
    private var sessionLoaded = false

    @Volatile
    private var isVibrationActive = true

    private var pendingZikrName: String? = null
    private var pendingIncrements = 0
    private var flushJob: Job? = null

    private val stats = combine(repository.dailyStats, repository.totalAllTime) { daily, total ->
        daily to total
    }

    val uiState: StateFlow<MisbahaUiState> = combine(
        selectedZikr,
        count,
        repository.customAzkar,
        stats
    ) { zikr, currentCount, custom, (daily, total) ->
        MisbahaUiState(
            selectedZikr = zikr,
            currentCount = currentCount,
            customAzkar = custom,
            dailyStats = daily,
            totalCountAllTime = total
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MisbahaUiState()
    )

    init {
        viewModelScope.launch {
            selectedZikr.value = repository.selectedZikr.first()
            count.value = repository.currentCount.first()
            sessionLoaded = true
        }
        viewModelScope.launch {
            settingsRepository.isVibrationEnabled.collect { isVibrationActive = it }
        }
    }

    fun onAction(action: MisbahaAction) {
        when (action) {
            MisbahaAction.Increment -> increment()
            MisbahaAction.Reset -> reset()
            is MisbahaAction.SelectZikr -> selectZikr(action.name)
            is MisbahaAction.AddCustomZikr -> addCustomZikr(action.name, action.onAdded)
            is MisbahaAction.UpdateCustomZikr -> updateCustomZikr(action.id, action.newName)
            is MisbahaAction.DeleteCustomZikr -> deleteCustomZikr(action.id)
            MisbahaAction.ClearHistory -> clearHistory()
        }
    }

    private fun increment() {
        vibrationHelper.vibrateTick(isVibrationActive)
        if (!sessionLoaded) return

        val zikr = selectedZikr.value
        count.update { it + 1 }
        persistSession()

        if (pendingZikrName != null && pendingZikrName != zikr) flushPending()
        pendingZikrName = zikr
        pendingIncrements++

        flushJob?.cancel()
        flushJob = viewModelScope.launch {
            delay(FLUSH_DELAY_MS)
            flushPending()
        }
    }

    private fun selectZikr(name: String) {
        flushNow()
        selectedZikr.value = name
        count.value = 0
        persistSession()
    }

    private fun reset() {
        flushNow()
        count.value = 0
        persistSession()
    }

    private fun addCustomZikr(name: String, onAdded: () -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addCustomZikr(trimmed)
            selectZikr(trimmed)
            onAdded()
        }
    }

    private fun updateCustomZikr(id: Int, newName: String) {
        viewModelScope.launch {
            val old = uiState.value.customAzkar.find { it.id == id }
            repository.updateCustomZikr(id, newName)
            if (old != null && old.name == selectedZikr.value) {
                selectedZikr.value = newName
                persistSession()
            }
        }
    }

    private fun deleteCustomZikr(id: Int) {
        viewModelScope.launch {
            val toDelete = uiState.value.customAzkar.find { it.id == id }
            repository.deleteCustomZikr(id)
            if (toDelete != null && toDelete.name == selectedZikr.value) {
                selectZikr(DefaultAzkar.first())
            }
        }
    }

    private fun clearHistory() {
        flushJob?.cancel()
        pendingZikrName = null
        pendingIncrements = 0
        count.value = 0
        persistSession()
        viewModelScope.launch { repository.clearAllHistory() }
    }

    private fun flushNow() {
        flushJob?.cancel()
        flushPending()
    }

    private fun flushPending() {
        val zikr = pendingZikrName ?: return
        val delta = pendingIncrements
        pendingZikrName = null
        pendingIncrements = 0
        if (delta > 0) {
            // appScope: الكتابة تكمل حتى لو الـ ViewModel اتقفل في نفس اللحظة.
            appScope.launch { repository.recordHistoryDelta(zikr, delta) }
        }
    }

    private fun persistSession() {
        val zikr = selectedZikr.value
        val current = count.value
        viewModelScope.launch { repository.saveCurrentSession(zikr, current) }
    }

    override fun onCleared() {
        flushNow()
        super.onCleared()
    }

    private companion object {
        const val FLUSH_DELAY_MS = 800L
    }
}
