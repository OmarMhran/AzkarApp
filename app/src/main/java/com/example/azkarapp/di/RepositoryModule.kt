package com.example.azkarapp.di

import com.example.azkarapp.data.repo.AzkarProgressRepository
import com.example.azkarapp.data.repo.AzkarProgressRepositoryImpl
import com.example.azkarapp.data.repo.AzkarRepositoryImpl
import com.example.azkarapp.data.repo.CountryRepositoryImpl
import com.example.azkarapp.data.repo.HomeRepository
import com.example.azkarapp.data.repo.HomeRepositoryImpl
import com.example.azkarapp.data.repo.MisbahaRepository
import com.example.azkarapp.data.repo.MisbahaRepositoryImpl
import com.example.azkarapp.data.repo.OnboardingRepository
import com.example.azkarapp.data.repo.OnboardingRepositoryImpl
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.data.repo.PrayerRepositoryImpl
import com.example.azkarapp.data.repo.QiblahRepository
import com.example.azkarapp.data.repo.QiblahRepositoryImpl
import com.example.azkarapp.data.repo.SettingsRepository
import com.example.azkarapp.data.repo.SettingsRepositoryImpl
import com.example.azkarapp.domain.repository.AzkarRepository
import com.example.azkarapp.domain.repository.CountryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAzkarRepository(impl: AzkarRepositoryImpl): AzkarRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCountryRepository(impl: CountryRepositoryImpl): CountryRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository

    @Binds
    @Singleton
    abstract fun bindMisbahaRepository(impl: MisbahaRepositoryImpl): MisbahaRepository

    @Binds
    @Singleton
    abstract fun bindPrayerRepository(impl: PrayerRepositoryImpl): PrayerRepository

    @Binds
    @Singleton
    abstract fun bindQiblahRepository(impl: QiblahRepositoryImpl): QiblahRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindAzkarProgressRepository(impl: AzkarProgressRepositoryImpl): AzkarProgressRepository
}
