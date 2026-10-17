package com.example.azkarapp.data.repo

import com.example.azkarapp.data.local.assets.JsonAssetReader
import com.example.azkarapp.domain.model.Country
import com.example.azkarapp.domain.repository.CountryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val jsonAssetReader: JsonAssetReader
) : CountryRepository {

    override suspend fun getAllCountries(): List<Country> {
        return jsonAssetReader.readCountries()
    }
}