package com.example.azkarapp.presentation.permission_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.PrayerRepository
import com.example.azkarapp.domain.model.Country
import com.example.azkarapp.domain.repository.CountryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val prayerRepository: PrayerRepository
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _countries.value = countryRepository.getAllCountries()
        }
    }

    fun saveSelectedCountry(
        country: Country,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            prayerRepository.saveCountrySelection(country.code, country.methodId)   // بدل prayerPreferences...
            onSaved()
        }
    }

    fun suggestCountry(countries: List<Country>): Country {
        val tzId = java.util.TimeZone.getDefault().id
        return countries.firstOrNull { it.code != "OTHER" && it.timeZoneId == tzId }
            ?: countries.firstOrNull { it.code == "EG" }
            ?: countries.first()
    }
}