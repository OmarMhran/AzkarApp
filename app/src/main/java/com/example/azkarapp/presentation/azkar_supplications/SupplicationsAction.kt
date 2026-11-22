package com.example.azkarapp.presentation.azkar_supplications

sealed interface SupplicationsAction {
    data class Tap(val supplicationId: Int, val onFinished: () -> Unit = {}) : SupplicationsAction
    data object DismissCompletionDialog : SupplicationsAction
    data object Retry : SupplicationsAction
}
