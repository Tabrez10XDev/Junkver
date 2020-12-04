package com.example.junkver.service
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.example.junkver.app.Dashboard
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

//class FirebaseService :  FirebaseMessagingService() {
//
//    override fun onMessageReceived(message: RemoteMessage?) {
//        super.onMessageReceived(message)
//
//        val intent = Intent(this, Dashboard::class.java)
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationID = Random.nextInt()
//
////        intent.addFlags(Intent. )
//    }
//}