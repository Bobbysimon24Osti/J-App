package com.osti.juniorapp.notifiche

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.osti.juniorapp.R
import com.osti.juniorapp.activity.LoginActivity
import com.osti.juniorapp.activity.MainActivity


class JuniorNotifiche(context:Context) {

    private val CHANNEL_ID = "JUNIORAPP"

    val intent = Intent(context, MainActivity::class.java)


    lateinit var action1PendingIntent: PendingIntent

    private lateinit var builder: NotificationCompat.Builder


    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    init{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("notifica", "notifica")
            action1PendingIntent = PendingIntent.getActivity(
                context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Nuova notifica")
                .setContentIntent(action1PendingIntent)
                .setAutoCancel(true)

            val channel = NotificationChannel(CHANNEL_ID, "Canale Junior", NotificationManager.IMPORTANCE_DEFAULT)

            if(!manager.notificationChannels.contains(channel)){
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotifica(){
        manager.notify(0 , builder.build())
    }


}