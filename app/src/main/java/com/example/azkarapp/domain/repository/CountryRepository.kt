package com.example.azkarapp.domain.repository

import com.example.azkarapp.domain.model.Country

interface CountryRepository {
    suspend fun getAllCountries(): List<Country>
}