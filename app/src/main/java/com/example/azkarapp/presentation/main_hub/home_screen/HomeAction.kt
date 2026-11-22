package com.example.azkarapp.presentation.main_hub.home_screen

sealed interface HomeAction {
    /** سحب للتحديث: محتوى جديد + إعادة تحميل المواقيت. */
    data object Refresh : HomeAction
}
