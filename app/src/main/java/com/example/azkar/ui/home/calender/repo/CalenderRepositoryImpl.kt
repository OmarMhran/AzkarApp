package com.example.azkar.ui.home.calender.repo

import com.example.azkar.ui.home.calender.data.CalenderDataClass
import com.example.azkar.util.shared.SharedPreferenceDataSource
import java.util.ArrayList
import javax.inject.Inject

class CalenderRepositoryImpl @Inject constructor(
    private val data : CalenderDataClass
) : CalenderRepository{

    override fun getHijriDate(): String = data.getIslamicDate()

    override fun getGeoDate(): String = data.getGoeDate()

    override fun getPrayerTimes() = data.getPrayerTimes()

    override fun getNextPrayerTime() = data.getNextPrayer()

    override fun getCountryName() = data.getCountryName()

    override fun setPrayerTimesForAlarm(): ArrayList<String> = data.setPrayerTimesForAlarm()
}