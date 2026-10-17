package com.example.azkarapp.data.local

import android.content.Context
import java.util.*
import kotlin.math.*

class PrayerTimesCalculator {

    private var calcMethod = EGYPT
    private var asrJuristic = SHAFII
    private var dhuhrMinutes = 0
    private var adjustHighLats = ANGLE_BASED
    private var timeFormat = TIME_12

    private var lat = 0.0
    private var lng = 0.0
    private var timeZone = 0.0
    private var jDate = 0.0

    private val timeNames =
        arrayListOf("الفجر", "الشروق", "الظهر", "العصر", "الغروب", "المغرب", "العشاء")
    private val invalidTime = "-----"
    private val numIterations = 1
    private var offsets = IntArray(7)
    private val methodParams = HashMap<Int, DoubleArray>()

    init {
        methodParams[JAFARI] = doubleArrayOf(16.0, 0.0, 4.0, 0.0, 14.0)
        methodParams[KARACHI] = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0)
        methodParams[ISNA] = doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0)
        methodParams[MWL] = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams[MAKKAH] = doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0)
        methodParams[EGYPT] = doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5)
        methodParams[TEHRAN] = doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
        methodParams[GULF] = doubleArrayOf(19.5, 0.0, 0.0, 0.0, 90.0)
        methodParams[KUWAIT] = doubleArrayOf(18.0, 0.0, 0.0, 0.0, 17.5)
        methodParams[QATAR] = doubleArrayOf(18.0, 0.0, 0.0, 0.0, 90.0)
        methodParams[SINGAPORE] = doubleArrayOf(20.0, 0.0, 0.0, 0.0, 18.0)
        methodParams[TURKEY] = doubleArrayOf(18.0, 0.0, 0.0, 0.0, 17.0)
        methodParams[JAKIM] = doubleArrayOf(20.0, 0.0, 0.0, 0.0, 18.0)
        methodParams[CUSTOM] = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
    }

    private fun fixangle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixhour(a: Double): Double {
        var hour = a - 24.0 * floor(a / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun radiansToDegrees(alpha: Double) = (alpha * 180.0) / Math.PI
    private fun degreesToRadians(alpha: Double) = (alpha * Math.PI) / 180.0

    private fun dsin(d: Double) = sin(degreesToRadians(d))
    private fun dcos(d: Double) = cos(degreesToRadians(d))
    private fun dtan(d: Double) = tan(degreesToRadians(d))
    private fun darcsin(x: Double) = radiansToDegrees(asin(x))
    private fun darccos(x: Double) = radiansToDegrees(acos(x))
    private fun darctan2(y: Double, x: Double) = radiansToDegrees(atan2(y, x))
    private fun darccot(x: Double) = radiansToDegrees(atan2(1.0, x))

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): DoubleArray {
        val d = jd - 2451545.0
        val g = fixangle(357.529 + 0.98560028 * d)
        val q = fixangle(280.459 + 0.98564736 * d)
        val l = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)))
        val e = 23.439 - (0.00000036 * d)
        val dec = darcsin(dsin(e) * dsin(l))
        var ra = darctan2(dcos(e) * dsin(l), dcos(l)) / 15.0
        ra = fixhour(ra)
        val eqT = q / 15.0 - ra
        return doubleArrayOf(dec, eqT)
    }

    private fun computeMidDay(t: Double): Double {
        val eqT = sunPosition(jDate + t)[1]
        return fixhour(12.0 - eqT)
    }

    private fun computeTime(g: Double, t: Double): Double {
        val d = sunPosition(jDate + t)[0]
        val z = computeMidDay(t)
        val beg = -dsin(g) - dsin(d) * dsin(lat)
        val mid = dcos(d) * dcos(lat)
        val v = darccos(beg / mid) / 15.0
        return z + if (g > 90) -v else v
    }

    private fun computeAsr(step: Double, t: Double): Double {
        val d = sunPosition(jDate + t)[0]
        val g = -darccot(step + dtan(abs(lat - d)))
        return computeTime(g, t)
    }

    fun tune(offsetTimes: IntArray) {
        for (i in offsetTimes.indices) {
            if (i < this.offsets.size) {
                this.offsets[i] = offsetTimes[i]
            }
        }
    }

    fun getPrayerTimes(
        date: Calendar,
        latitude: Double,
        longitude: Double,
        tZone: Double
    ): LinkedHashMap<String, String> {
        this.lat = latitude
        this.lng = longitude
        this.timeZone = tZone

        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DATE)

        this.jDate = julianDate(year, month, day)
        val lonDiff = longitude / (15.0 * 24.0)
        this.jDate -= lonDiff

        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0)
        for (i in 1..numIterations) {
            times = computeTimes(times)
        }

        times = adjustTimes(times)
        times = tuneTimes(times)

        val result = linkedMapOf<String, String>()
        val pNames = arrayListOf("الفجر", "الشروق", "الظهر", "العصر", "الغروب", "المغرب", "العشاء")

        for (i in 0..6) {
            val timeStr = floatToTime12(times[i])
            if (pNames[i] != "الشروق" && pNames[i] != "الغروب") {
                result[pNames[i]] = timeStr
            }
        }
        return result
    }

    private fun computeTimes(times: DoubleArray): DoubleArray {
        val t = dayPortion(times)
        val params = methodParams[calcMethod] ?: doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)

        val fajr = computeTime(180.0 - params[0], t[0])
        val sunrise = computeTime(180.0 - 0.833, t[1])
        val dhuhr = computeMidDay(t[2])
        val asr = computeAsr(1.0 + asrJuristic, t[3])
        val sunset = computeTime(0.833, t[4])

        // حساب المغرب: لو المعامل الثاني يساوي 1، يحسب بعد الغروب بإزاحة دقائق معينة
        val maghrib = if (params[1] == 1.0) {
            sunset + params[2] / 60.0
        } else {
            computeTime(params[2], t[5])
        }

        // حساب العشاء: لو القيمة أكبر من 60 فهذا يعني أنها دقائق ثابتة بعد المغرب (مثل 90 دقيقة في قطر)
        val isha = if (params[4] > 60.0) {
            maghrib + params[4] / 60.0
        } else {
            computeTime(params[4], t[6])
        }

        return doubleArrayOf(fajr, sunrise, dhuhr, asr, sunset, maghrib, isha)
    }

    private fun adjustTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) {
            times[i] += timeZone - (lng / 15.0)
        }
        adjustHighLatTimes(times)
        times[2] += dhuhrMinutes / 60.0
        return times
    }

    private fun adjustHighLatTimes(times: DoubleArray) {
        if (adjustHighLats == NONE) return
        val params = methodParams[calcMethod] ?: return

        val sunrise = times[1]
        val sunset = times[4]
        if (sunrise.isNaN() || sunset.isNaN()) return   // نهار/ليل قطبي: مفيش حل بالنسب

        val night = fixhour(sunrise - sunset)

        // الفجر: دايماً بالزاوية
        times[0] = adjustHLTime(times[0], sunrise, params[0], night, counterClockwise = true)
        // العشاء: بس لو بالزاوية (مش دقائق ثابتة زي مكة وقطر)
        if (params[4] <= 60.0) {
            times[6] = adjustHLTime(times[6], sunset, params[4], night, counterClockwise = false)
        }
    }

    private fun adjustHLTime(
        time: Double,
        base: Double,
        angle: Double,
        night: Double,
        counterClockwise: Boolean
    ): Double {
        val portion = nightPortion(angle, night)
        val diff = if (counterClockwise) fixhour(base - time) else fixhour(time - base)
        return if (time.isNaN() || diff > portion) {
            if (counterClockwise) base - portion else base + portion
        } else {
            time
        }
    }

    private fun nightPortion(angle: Double, night: Double): Double {
        val portion = when (adjustHighLats) {
            ANGLE_BASED -> angle / 60.0
            ONE_SEVENTH -> 1.0 / 7.0
            else -> 1.0 / 2.0                  // MID_NIGHT
        }
        return portion * night
    }


    private fun tuneTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) {
            times[i] = times[i] + this.offsets[i] / 60.0
        }
        return times
    }

    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6) times[i] /= 24.0
        return times
    }

    private fun floatToTime12(time: Double): String {
        if (time.isNaN()) return invalidTime
        val roundedTime = fixhour(time + 0.5 / 60.0)
        var hours = floor(roundedTime).toInt()
        val minutes = floor((roundedTime - hours) * 60.0).toInt()

        val suffix = if (hours >= 12) "م" else "ص"
        hours = ((hours + 12 - 1) % 12) + 1

        return String.format(
            Locale.US,
            "%02d:%02d %s",
            hours,
            minutes,
            suffix
        )   // كان Locale.getDefault()
    }

    companion object {
        const val JAFARI = 0
        const val KARACHI = 1
        const val ISNA = 2
        const val MWL = 3
        const val MAKKAH = 4
        const val EGYPT = 5
        const val TEHRAN = 6
        const val GULF = 7
        const val KUWAIT = 8
        const val QATAR = 9
        const val SINGAPORE = 10
        const val TURKEY = 11
        const val JAKIM = 12
        const val CUSTOM = 13

        const val SHAFII = 0
        const val HANAFI = 1

        const val NONE = 0
        const val MID_NIGHT = 1
        const val ONE_SEVENTH = 2
        const val ANGLE_BASED = 3

        const val TIME_24 = 0
        const val TIME_12 = 1

        @JvmStatic
        fun getPrayerTimes(
            lat: Double,
            lng: Double,
            method: Int = EGYPT,
            juristic: Int = SHAFII,
            customOffsets: IntArray? = null,
            timeZoneId: String? = null,
            dayOffset: Int = 0,
            baseDate: Calendar = Calendar.getInstance()
        ): LinkedHashMap<String, String> {

            val cal = baseDate.clone() as Calendar

            val tz = if (!timeZoneId.isNullOrEmpty()) {
                TimeZone.getTimeZone(timeZoneId)
            } else {
                TimeZone.getDefault()
            }
            cal.timeZone = tz

            if (dayOffset != 0) cal.add(Calendar.DAY_OF_MONTH, dayOffset)

            val tzOffsetMs = tz.getOffset(cal.timeInMillis)
            val timezone = tzOffsetMs.toDouble() / (1000 * 60 * 60)

            val calculator = PrayerTimesCalculator()
            calculator.calcMethod = method
            calculator.asrJuristic = juristic
            if (customOffsets != null) {
                calculator.tune(customOffsets)
            }

            return calculator.getPrayerTimes(cal, lat, lng, timezone)
        }
    }
}