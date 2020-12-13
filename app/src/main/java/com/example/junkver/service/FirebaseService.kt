package com.example.junkver.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseService :  FirebaseMessagingService() {


    val auth = FirebaseAuth.getInstance()



    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Log.d("POCHA","BEGIN")
        val messages = message?.data

        val sharedPref = getSharedPreferences("notificationPref",Context.MODE_PRIVATE)
        val unseenPref = getSharedPreferences("unseenPref",Context.MODE_PRIVATE)


        val username = auth.currentUser?.displayName
        val currentID = sharedPref.getString("currentID","")
        val notificationPref = sharedPref.getInt( messages?.get("fromServer"),1)
        Log.d("PLEASE",messages?.get("fromServer").toString())
        if (messages?.get("body") != username && messages?.get("fromServer") != currentID) {
            val old =  unseenPref.getInt(messages?.get("fromServer").toString(),0)
            if(old < 10){
                with(unseenPref?.edit()){
                    this?.putInt(messages?.get("fromServer").toString(),old+1)
                    this?.apply()
                }
            }
            if(notificationPref == 1) {
            val intent = Intent(this, Dashboard::class.java)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

//            val channelID = messages?.get("fromServer").toString()
            Log.d("Response", messages.toString())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
            Log.d("POCHA","poda dei")
             val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
            val notification = NotificationCompat.Builder(this, "Main")
                .setContentTitle(messages?.get("title"))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo1)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(messages?.get("body") + ": " + messages?.get("message"))
                .build()

            notificationManager.notify(notificationID , notification)
        } }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName = "Junkver"
        val channel = NotificationChannel("Main",channelName, IMPORTANCE_HIGH).apply {
            description = "MainChannel"
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
        }

        notificationManager.createNotificationChannel(channel)
    }



}