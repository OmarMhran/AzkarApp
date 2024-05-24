package com.example.azkar.ui.home.calender.data

import android.location.Location
import com.example.azkar.models.Prayer
import com.example.azkar.util.PrayerTimes
import com.example.azkar.util.shared.SharedPreferenceDataSource
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.hours

class CalenderDataClass @Inject constructor(
    private val shared: SharedPreferenceDataSource
) {

    @Inject
    lateinit var ummalquraCalendar: UmmalquraCalendar

    @Inject
    lateinit var prayerTimes: PrayerTimes


    val latitude = (shared.getUserLatitude()).toDouble()
    val longitude = (shared.getUserLongitude()).toDouble()
    val altitude = shared.getUserAltitude()

    val mGMToffset = GregorianCalendar().timeZone.rawOffset
    val gmtDiff = TimeUnit.HOURS.convert(mGMToffset.toLong(),TimeUnit.MILLISECONDS)


    fun getCountryName() = shared.getUserCountry()

    fun getGoeDate(): String {
        val ar = Locale("ar")
        val goeCalendar = Calendar.getInstance(ar)
        val year =
            goeCalendar.get(Calendar.YEAR).toString()                                  // 1436
        val month =
            goeCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, ar) // جمادى الأولى
        val dayNum = goeCalendar.get(Calendar.DAY_OF_MONTH)
        val day = goeCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, ar)

        return "$day, $dayNum $month $year"
    }

    fun getIslamicDate(): String {
        val ar = Locale("ar")
        ummalquraCalendar = UmmalquraCalendar(ar)
        val year =
            ummalquraCalendar.get(Calendar.YEAR).toString()                                  // 1436
        val month =
            ummalquraCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, ar) // جمادى الأولى
        val dayNum = ummalquraCalendar.get(Calendar.DAY_OF_MONTH)


        return "$dayNum $month $year"
    }


    fun setPrayerTimes(): ArrayList<String> {
        val calender = Calendar.getInstance()
        prayerTimes = PrayerTimes()
        prayerTimes.timeFormat = PrayerTimes.TIME_12
        prayerTimes.calcMethod = PrayerTimes.EGYPT
        val times = prayerTimes.getPrayerTimes(calender, latitude, longitude, gmtDiff.toDouble())
        return times
    }


    fun setPrayerTimesForAlarm(): ArrayList<String> {
        val calender = Calendar.getInstance()
        prayerTimes = PrayerTimes()
        prayerTimes.timeFormat = PrayerTimes.TIME_24
        prayerTimes.calcMethod = PrayerTimes.EGYPT
        val times = prayerTimes.getPrayerTimes(calender, latitude, longitude, gmtDiff.toDouble())
        return times
    }

    fun getPrayerTimes(): ArrayList<Prayer> {
        val times = setPrayerTimes()
        val names = prayerTimes.timeNames
        val prayerList = ArrayList<Prayer>()
        val prayerImage = prayerTimes.prayerImages
        var i = 0
        while (i < 6) {
            prayerList.add(i, Prayer(i, names.get(i), times.get(i), prayerImage.get(i), true))
            i++
        }
        return prayerList

    }




    fun getNextPrayer(): Prayer {
        val sdf: DateFormat = SimpleDateFormat("h:mm a", Locale("ar"))
        val times = setPrayerTimes()
        val currentTimeString = sdf.format(Calendar.getInstance().time)
        val currentTime2 = sdf.parse(currentTimeString)!!.time

        val fajr = sdf.parse(times.get(0))!!.time
        val shrouk = sdf.parse(times.get(1))!!.time
        val thuhr = sdf.parse(times.get(2))!!.time
        val asr = sdf.parse(times.get(3))!!.time
        val maghrib = sdf.parse(times.get(4))!!.time
        val ishaa = sdf.parse(times.get(5))!!.time

        if (currentTime2 > fajr && (currentTime2 <= shrouk)) {
            return getPrayerTimes()[1]
        } else if (currentTime2 > shrouk && (currentTime2 <= thuhr)) {
            return getPrayerTimes()[2]
        } else if (currentTime2 > thuhr && (currentTime2 <= asr)) {
            return getPrayerTimes()[3]
        } else if (currentTime2 > asr && (currentTime2 <= maghrib)) {
            return getPrayerTimes()[4]
        } else if (currentTime2 > maghrib && (currentTime2 <= ishaa)) {
            return getPrayerTimes()[5]
        } else {
            return getPrayerTimes()[0]
        }

    }
}