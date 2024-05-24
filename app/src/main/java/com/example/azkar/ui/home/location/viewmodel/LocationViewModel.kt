package com.example.azkar.ui.location.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.azkar.ui.location.repo.LocationRepository

class LocationViewModel @ViewModelInject constructor(
    private val locationRepository : LocationRepository
): ViewModel() {

    val _location : MutableLiveData<String> = MutableLiveData()
    val location : LiveData<String>
        get() = _location

    init {
        getUserCountry()
    }

    fun updateUserLatitude(latitude: String) = locationRepository.updateUserLatitude(latitude)
    fun updateUserLongitude(longitude: String) = locationRepository.updateUserLongitude(longitude)
    fun updateUserAltitude(altitude: String) = locationRepository.updateUserAltitude(altitude)
    fun updateUserCountry(country: String) = locationRepository.updateUserCountry(country)

    private fun getUserCountry() {
        _location.value = locationRepository.getUserCountry()
    }
}