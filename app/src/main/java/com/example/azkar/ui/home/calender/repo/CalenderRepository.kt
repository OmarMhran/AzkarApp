package com.example.azkar.ui.home.calender.repo

import com.example.azkar.models.Prayer
import java.util.ArrayList

interface CalenderRepository {

    // get Dates
    fun getHijriDate() : String
    fun getGeoDate() : String

    // get Prayer Times
    fun getPrayerTimes(): ArrayList<Prayer>
    fun getNextPrayerTime(): Prayer
    fun setPrayerTimesForAlarm(): ArrayList<String>

    // get Country
    fun getCountryName(): String
}