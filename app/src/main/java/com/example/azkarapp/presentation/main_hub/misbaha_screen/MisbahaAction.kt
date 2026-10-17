package com.example.azkarapp.presentation.main_hub.misbaha_screen

sealed interface MisbahaAction {
    data object Increment : MisbahaAction
    data object Reset : MisbahaAction
    data class SelectZikr(val name: String) : MisbahaAction

    data class AddCustomZikr(val name: String, val onAdded: () -> Unit) : MisbahaAction
    data class UpdateCustomZikr(val id: Int, val newName: String) : MisbahaAction
    data class DeleteCustomZikr(val id: Int) : MisbahaAction

    data object ClearHistory : MisbahaAction
}
