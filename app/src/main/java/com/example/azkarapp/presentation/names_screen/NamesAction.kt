package com.example.azkarapp.presentation.names_screen

import com.example.azkarapp.domain.model.Names

sealed interface NamesAction {
    data class SearchChanged(val query: String) : NamesAction
    data class SelectName(val name: Names) : NamesAction
    data object DismissName : NamesAction
    data object Retry : NamesAction
}
