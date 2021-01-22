package com.yeocak.chatapp

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yeocak.chatapp.activities.LoginActivity
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.activities.MessageActivity.Companion.ctx
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {

    val channelId = "message_channel"

    override fun onMessageReceived(message: RemoteMessage) {

        super.onMessageReceived(message)

        val intent = Intent(this, LoginActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.com_facebook_button_icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)

        // Taking the messages:

        DatabaseFun.setup("from${message.data["fromUID"]}")
        DatabaseFun.add("from${message.data["fromUID"]}", message.data["message"]!!,false)

        try{

        }catch (e: Exception){
            Log.d("Testing",e.toString())
        }

        Log.d("Testing","Worked?")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

}