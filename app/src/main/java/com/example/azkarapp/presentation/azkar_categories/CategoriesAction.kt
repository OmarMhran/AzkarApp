package com.example.azkarapp.presentation.azkar_categories

sealed interface CategoriesAction {
    data object Retry : CategoriesAction
}
