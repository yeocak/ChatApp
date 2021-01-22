package com.yeocak.chatapp.activities

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.yeocak.chatapp.*
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var adapting : MessagingAdapter
    private lateinit var messageList : MutableList<SingleMessage>
    private lateinit var auth : FirebaseUser
    private lateinit var db : FirebaseFirestore

    private lateinit var partnerName : String
    private lateinit var partnerPhone : String

    private lateinit var uid: String

    companion object {
        lateinit var ctx : Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth.currentUser!!

        ctx = this

        uid = intent.getStringExtra("uid")!!

        Log.e("Sending","$uid ,")

        val db = FirebaseFirestore.getInstance().collection("profile").document(uid)

        db.get()
                .addOnSuccessListener {
                    partnerName = it["name"].toString()
                    partnerPhone = it["currentPhone"].toString()
                }

        DatabaseFun.setup("from$uid")
        messageList = DatabaseFun.take("from$uid")

        adapting = MessagingAdapter(messageList)

        binding.rvMessaging.adapter = adapting
        binding.rvMessaging.layoutManager = LinearLayoutManager(this)
        binding.rvMessaging.scrollToPosition(messageList.size-1)

        binding.btnSendMessage.setOnClickListener {
            if(binding.etNewMessage.text.isNotEmpty()){
                val newMessage = binding.etNewMessage.text.toString()

                sendNotification(
                        PushNotification(
                                NotificationData(auth.displayName!!, newMessage, auth.uid),
                                partnerPhone
                        )
                )
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitObject.api.postNotification(notification)
            if(response.isSuccessful) {
                updateRV()
                Log.e("Sending","Sended?")
            } else {
                Log.e("Sending", "Error 1 : ${response.errorBody().toString()}")

            }
        } catch(e: Exception) {
            Log.e("Sending", "Error 2 : $e")
        }
    }

    fun updateRV(){
        DatabaseFun.add("from$uid", binding.etNewMessage.text.toString(),true)

        messageList.clear()
        messageList.addAll(DatabaseFun.take("from$uid"))

        adapting.notifyDataSetChanged()
        binding.rvMessaging.scrollToPosition(messageList.size-1)
    }

}