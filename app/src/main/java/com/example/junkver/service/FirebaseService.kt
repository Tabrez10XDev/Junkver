package com.example.junkver.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.android.gms.flags.Flag
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.internal.notify
import kotlin.random.Random

class FirebaseService :  FirebaseMessagingService() {



    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)



        val intent = Intent(this, Dashboard::class.java)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            val messages = message?.data
        Log.d("Response",messages.toString())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
                val notification = NotificationCompat.Builder(this, "myChannel")
                    .setContentTitle(messages?.get("title"))
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.logo1)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentText(messages?.get("message"))
                    .build()

                notificationManager.notify(notificationID, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName = "channel"
        val channel = NotificationChannel("myChannel",channelName, IMPORTANCE_HIGH).apply {
            description = "description"
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
        }

        notificationManager.createNotificationChannel(channel)
    }



}