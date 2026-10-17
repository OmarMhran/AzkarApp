package com.example.azkarapp.presentation.splash_screen

import androidx.lifecycle.ViewModel
import com.example.azkarapp.data.repo.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository
) : ViewModel() {

    suspend fun nextRoute(): String =
        if (prayerRepository.isOnboardingDone()) "main_hub" else "permission_screen"
}