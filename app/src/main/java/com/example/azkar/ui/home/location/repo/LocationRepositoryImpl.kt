package com.example.azkar.ui.location.repo

import com.example.azkar.util.shared.SharedPreferenceDataSource
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val sharedPreferenceDataSource: SharedPreferenceDataSource
) : LocationRepository {
    override fun updateUserLatitude(latitude: String) = sharedPreferenceDataSource.setUserLatitude(latitude)

    override fun updateUserLongitude(longitude: String) = sharedPreferenceDataSource.setUserLongitude(longitude)

    override fun updateUserAltitude(altitude: String) = sharedPreferenceDataSource.setUserAltitude(altitude)

    override fun updateUserCountry(country: String) = sharedPreferenceDataSource.setUserCountry(country)

    override fun getUserCountry(): String = sharedPreferenceDataSource.getUserCountry()

    override fun getUserLatitude(): String = sharedPreferenceDataSource.getUserLatitude()

    override fun getUserLongitude(): String = sharedPreferenceDataSource.getUserLongitude()

    override fun getUserAltitude(): String = sharedPreferenceDataSource.getUserAltitude()

}