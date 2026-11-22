package com.example.azkarapp.presentation.splash_screen

import androidx.lifecycle.ViewModel
import com.example.azkarapp.data.repo.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class SplashDestination { MainHub, Permission }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    suspend fun resolveDestination(): SplashDestination =
        if (onboardingRepository.isOnboardingDone()) SplashDestination.MainHub
        else SplashDestination.Permission
}
