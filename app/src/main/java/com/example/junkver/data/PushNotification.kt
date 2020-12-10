package com.example.junkver.data

data class PushNotification(
    val data : NotificationData,
    val to : String,
    val collapse_key : String = "type_a"
)