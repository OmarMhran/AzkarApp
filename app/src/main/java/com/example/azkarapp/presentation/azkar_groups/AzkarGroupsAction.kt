package com.example.azkarapp.presentation.azkar_groups

sealed interface AzkarGroupsAction {
    data object Retry : AzkarGroupsAction
}
