package com.example.azkar.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import com.example.azkar.R
import com.example.azkar.util.Constants
import com.example.azkar.util.Constants.Companion.ACTION_STOP_SERVICE


class MyService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer= MediaPlayer.create(this, R.raw.alarm)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) {
            return START_REDELIVER_INTENT;
        }


        val action = intent.action


        when(action){
             ACTION_STOP_SERVICE ->{
                 mediaPlayer?.stop();
                 mediaPlayer?.reset();
                 mediaPlayer?.release();
                 mediaPlayer=null;
              NotificationUtils().clearNotifications(this)
             }else -> {
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
             }

        }

        return START_NOT_STICKY;
    }


    override fun onDestroy() {
        super.onDestroy()
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer=null;
    }
}