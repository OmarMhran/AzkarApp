package com.example.azkarapp.presentation.permission_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azkarapp.data.repo.OnboardingRepository
import com.example.azkarapp.domain.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        loadCountries()
    }

    fun onAction(action: PermissionAction) {
        when (action) {
            is PermissionAction.SaveCountry -> saveCountry(action.country, action.onSaved)
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val countries = onboardingRepository.getCountries()
            _uiState.value = PermissionUiState(
                countries = countries,
                suggestedCountry = suggestCountry(countries)
            )
        }
    }

    private fun saveCountry(country: Country, onSaved: () -> Unit) {
        viewModelScope.launch {
            onboardingRepository.saveCountrySelection(country)
            onSaved()
        }
    }

    private fun suggestCountry(countries: List<Country>): Country? {
        val tzId = TimeZone.getDefault().id
        return countries.firstOrNull { it.code != "OTHER" && it.timeZoneId == tzId }
            ?: countries.firstOrNull { it.code == "EG" }
            ?: countries.firstOrNull()
    }
}
