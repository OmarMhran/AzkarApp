package com.example.azkar.util.shared

interface SharedPreferenceDataSource  {

    fun setUserLatitude(latitude : String)
    fun setUserLongitude(longitude : String)
    fun setUserAltitude(altitude : String)
    fun setUserCountry(country : String)


    fun getUserLatitude() : String
    fun getUserLongitude(): String
    fun getUserAltitude(): String
    fun getUserCountry(): String



    fun getMisbahaCount(): Int
    fun setMisbahaCount(count: Int)
    fun isVibrationOn(): Boolean
    fun setVibrationOn(boolean: Boolean)


    fun getSelectedColors(): Int
    fun getSelectedTheme(): Int
    fun setSelectedColor(color: Int)
    fun setSelectedTheme(theme: Int)

}