package com.yeocak.chatapp

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.View.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yeocak.chatapp.activities.LoginActivity
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {

    private val channelId = "message_channel"

    override fun onMessageReceived(message: RemoteMessage) {

        super.onMessageReceived(message)

        val intent = Intent(this, LoginActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = 1

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        var photoPers: Bitmap? = null
        CoroutineScope(IO).launch {
            if(message.data["photo"] != "null"){
                photoPers = loadImage(message.data["photo"]!!)
            }
            else{
                photoPers = ContextCompat.getDrawable(this@MyFirebaseMessagingService,R.drawable.ic_baseline_groups_24)?.toBitmap()
            }

            withContext(Main){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(this@MyFirebaseMessagingService, 0, intent, FLAG_ONE_SHOT)
                val notification = NotificationCompat.Builder(this@MyFirebaseMessagingService, channelId)
                        .setContentTitle(message.data["title"])
                        .setContentText(message.data["message"])
                        .setSmallIcon(R.drawable.vector_icon_small)
                        .setLargeIcon(photoPers)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    notificationManager.notify(notificationID, notification)

                }
            }
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

    private suspend fun loadImage(photo : String): Bitmap{
        var bitmapNow: Bitmap? = null
        val request = ImageRequest.Builder(this)
                .data(photo)
                .target{
                    bitmapNow = (it as BitmapDrawable).bitmap
                }
                .build()

        imageLoader.execute(request)

        return bitmapNow!!
    }

}