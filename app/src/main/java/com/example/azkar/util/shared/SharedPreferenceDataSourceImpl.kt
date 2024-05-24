package com.example.azkar.util.shared

import android.content.SharedPreferences
import com.example.azkar.util.Constants.Companion.MISBAHA_COUNT
import com.example.azkar.util.Constants.Companion.SETTINGS_COLOR
import com.example.azkar.util.Constants.Companion.SETTINGS_THEME
import com.example.azkar.util.Constants.Companion.USER_ALTITUDE
import com.example.azkar.util.Constants.Companion.USER_COUNTRY
import com.example.azkar.util.Constants.Companion.USER_LATITUDE
import com.example.azkar.util.Constants.Companion.USER_LONGITUDE
import com.example.azkar.util.Constants.Companion.VIBRATION_ON
import javax.inject.Inject

class SharedPreferenceDataSourceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SharedPreferenceDataSource {

    override fun setUserLatitude(latitude: String) = sharedPreferences.edit().putString(USER_LATITUDE,latitude).apply()
    override fun setUserLongitude(longitude: String) = sharedPreferences.edit().putString(USER_LONGITUDE,longitude).apply()
    override fun setUserAltitude(altitude: String) = sharedPreferences.edit().putString(USER_ALTITUDE,altitude).apply()
    override fun setUserCountry(country: String) = sharedPreferences.edit().putString(USER_COUNTRY,country).apply()

    override fun getUserLatitude(): String = sharedPreferences.getString(USER_LATITUDE,"30.0444").toString()
    override fun getUserLongitude(): String = sharedPreferences.getString(USER_LONGITUDE, "31.2357").toString()
    override fun getUserAltitude(): String = sharedPreferences.getString(USER_ALTITUDE,"23").toString()
    override fun getUserCountry(): String = sharedPreferences.getString(USER_COUNTRY,"القاهرة، مصر").toString()

    //misbaha count
    override fun setMisbahaCount(count: Int) = sharedPreferences.edit().putInt(MISBAHA_COUNT, count).apply()
    override fun getMisbahaCount(): Int = sharedPreferences.getInt(MISBAHA_COUNT, 0)

    //vibration on misbaha
    override fun setVibrationOn(boolean: Boolean) = sharedPreferences.edit().putBoolean(VIBRATION_ON, boolean).apply()
    override fun isVibrationOn(): Boolean = sharedPreferences.getBoolean(VIBRATION_ON, true)


    //settings
    override fun getSelectedColors(): Int  = sharedPreferences.getInt(SETTINGS_COLOR,0)
    override fun getSelectedTheme(): Int = sharedPreferences.getInt(SETTINGS_THEME,0)

    override fun setSelectedColor(color: Int) = sharedPreferences.edit().putInt(SETTINGS_COLOR,color).apply()
    override fun setSelectedTheme(theme: Int) =sharedPreferences.edit().putInt(SETTINGS_THEME,theme).apply()
}