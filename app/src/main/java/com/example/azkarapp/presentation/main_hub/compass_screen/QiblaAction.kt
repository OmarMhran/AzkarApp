package com.example.azkarapp.presentation.main_hub.compass_screen

sealed interface QiblaAction {
    /** إعادة تحديد الموقع وحساب القبلة بعد فشل. */
    data object Retry : QiblaAction
}
