package com.example.junkver.data

import com.example.junkver.util.Constants.Companion.CONTENT_TYPE
import com.example.junkver.util.Constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification : PushNotification
    ) : Response<ResponseBody>






}