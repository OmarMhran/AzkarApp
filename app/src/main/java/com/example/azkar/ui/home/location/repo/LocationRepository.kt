package com.example.azkar.ui.location.repo

interface LocationRepository {
    fun updateUserLatitude(latitude : String)
    fun updateUserLongitude(longitude : String)
    fun updateUserAltitude(altitude : String)
    fun updateUserCountry(country : String)

    fun getUserCountry(): String
    fun getUserLatitude(): String
    fun getUserLongitude(): String
    fun getUserAltitude(): String
}