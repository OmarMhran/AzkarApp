package com.example.azkar.worker


import com.example.azkar.R
import com.example.azkar.ui.home.HomeActivity
import com.example.azkar.util.Constants
import com.example.azkar.util.Constants.Companion.TAG
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.azkar.services.MyService
import com.example.azkar.services.NotificationUtils
import java.util.*



class NotificationWorker(val context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {

        val prayingNum = inputData.getInt(AppWorker.PRAYER_NUM, 0)
        val prayerTime = inputData.getString(AppWorker.PRAYER_TIME)

        Log.d(TAG, "doWork: ")

        displayPrayerNotification(prayingNum,prayerTime!!)

        return Result.success()
    }

    private fun displayPrayerNotification(prayingNum: Int,prayerTime: String) {
        var prayingName = ""

        when (prayingNum) {
            0 -> prayingName = context.getString(R.string.fajr)
            1 -> prayingName = context.getString(R.string.sunrise)
            2 -> {
                val calendar = Calendar.getInstance()
                val day = calendar[Calendar.DAY_OF_WEEK]
                prayingName =
                    if (day == Calendar.FRIDAY) context.getString(R.string.jumaa) else context.getString(
                        R.string.dhuhr
                    )
            }

            3 -> prayingName = context.getString(R.string.asr)
            4 -> prayingName = context.getString(R.string.maghrib)
            5 -> prayingName = context.getString(R.string.isha)
        }

        Log.d(TAG, "displayPrayerNotification: $prayingName")
        val intent = PendingIntent.getActivity(
            context, 0, Intent(context, HomeActivity::class.java), 0)
        val builder =
            NotificationCompat.Builder(context, Constants.PRAYER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_app_round)
                .setContentText(prayingName)
                .setContentTitle("${context.getString(R.string.remember)} $prayingName $prayerTime")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLights(-0xff0100, 1000, 1000)
                .setAutoCancel(true)
                .setContentIntent(intent)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(prayingNum, builder.build())

//        val i = Intent(context, MyService::class.java)
//        context.startService(i)
//        NotificationUtils().showNotification(context,prayingName,prayerTime)

    }
}