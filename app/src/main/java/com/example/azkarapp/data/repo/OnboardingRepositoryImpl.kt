package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.PrayerPreferencesDataSource
import com.example.azkarapp.domain.model.Country
import com.example.azkarapp.domain.repository.CountryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val prayerPreferences: PrayerPreferencesDataSource,
    private val countryRepository: CountryRepository
) : OnboardingRepository {

    override suspend fun isOnboardingDone(): Boolean = prayerPreferences.isOnboardingDone()

    override suspend fun getCountries(): List<Country> = countryRepository.getAllCountries()

    override suspend fun saveCountrySelection(country: Country) {
        prayerPreferences.saveCountrySelection(country.code, country.methodId)
    }
}
