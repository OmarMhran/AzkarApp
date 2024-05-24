package com.example.azkar.worker


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.*
import com.example.azkar.ui.home.calender.repo.CalenderRepositoryImpl
import com.example.azkar.util.Constants.Companion.TAG
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.lang.System.currentTimeMillis


@Suppress("DEPRECATION")
class AppWorker @WorkerInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters,
    private val repo: CalenderRepositoryImpl
) : Worker(context, params) {


    override fun doWork(): Result {
        getPrayerTimes()
        return Result.success()
    }


    private fun getPrayerTimes() {
        val prayers = repo.setPrayerTimesForAlarm()
        //schedule notification
        scheduleNotification(prayers)
    }

    @SuppressLint("SimpleDateFormat")
    private fun scheduleNotification(prayers: ArrayList<String>) {
        for (i in prayers.indices) {

            val prayer = prayers[i]

            val currentTime = currentTimeMillis()
            val sdf = SimpleDateFormat("HH:mm")
            val time = sdf.parse(prayer)!!
            val hours = time.hours
            val min = time.minutes

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTime
            calendar[Calendar.HOUR_OF_DAY] = hours
            calendar[Calendar.MINUTE] = min
            calendar[Calendar.SECOND] = 1

            val customTime = calendar.timeInMillis

            if (customTime > currentTime) {
                val delay = customTime - currentTime

                val data = Data.Builder()
                    .putInt(PRAYER_NUM, i)
                    .putString(PRAYER_TIME, prayer)
                    .build()


                val work = OneTimeWorkRequest
                    .Builder(NotificationWorker::class.java)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

                val workManager = WorkManager.getInstance(context)
                workManager.enqueue(work)

                Log.d(TAG, "scheduleNotification: prayerisnow , $hours , $min")
            } else {
                Log.d(TAG, "scheduleNotification: current is greater than custom")
            }
        }
    }

    companion object {
        const val PRAYER_NUM = "prayer_num"
        const val PRAYER_TIME = "prayer_time"
    }
}

