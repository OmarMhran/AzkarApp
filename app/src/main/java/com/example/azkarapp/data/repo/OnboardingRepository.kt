package com.example.azkarapp.data.repo

import com.example.azkarapp.domain.model.Country

/** كل ما يخص أول تشغيل: اختيار البلد وهل المستخدم عدّى شاشة الأذونات. */
interface OnboardingRepository {
    suspend fun isOnboardingDone(): Boolean
    suspend fun getCountries(): List<Country>
    suspend fun saveCountrySelection(country: Country)
}
