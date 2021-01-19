package com.yeocak.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.yeocak.chatapp.NotificationData
import com.yeocak.chatapp.PushNotification
import com.yeocak.chatapp.R
import com.yeocak.chatapp.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class TesterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tester)

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/tester")

        val title = "Test Title"
        val message = "Test message"

        PushNotification(
            NotificationData(title,message),
            "/topics/tester"
        ).also {
            sendNotification(it)
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {

        try {

            val answer = RetrofitObject.api.postNotification(notification)

            if (answer.isSuccessful){
                Log.d("Firebasing","Evet:")
            }
            else{
                Log.d("Firebasing","Hayır: ${answer}")
            }

        } catch (e: Exception){
            e.printStackTrace()
            Log.d("Firebasing","Very no: ${e.stackTraceToString()}")
        }

    }
}