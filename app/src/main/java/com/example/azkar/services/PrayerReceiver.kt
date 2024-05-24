package com.example.azkar.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

//@AndroidEntryPoint
class PrayerReceiver : BroadcastReceiver() {

//    @Inject
   lateinit var builder :NotificationCompat.Builder


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        //create a date reference

        val i = Intent(context, MyService::class.java)
        context.startService(i)
        createNotification(context)
    }

    private fun createNotification(context: Context) {

        val calendar: Calendar = Calendar.getInstance()
        val formatDate: SimpleDateFormat = SimpleDateFormat(
            "EEEE, MMMM d, yyyy",
            Locale.getDefault()
        )
        val formatTime= SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = formatDate.format(calendar.time)
        val time = formatTime.format(calendar.time)


        NotificationUtils().showNotification(context,"حان وقت الصلاة","$date $time")



    }



}
