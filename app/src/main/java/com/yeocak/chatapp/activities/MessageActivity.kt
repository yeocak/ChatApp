package com.yeocak.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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

    private lateinit var partnerUid: String

    private lateinit var realtime : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        auth = Firebase.auth.currentUser!! // User auth
        partnerUid = intent.getStringExtra("uid")!! // Partner's uid


        val firestore = FirebaseFirestore.getInstance().collection("profile").document(partnerUid) // Getting profile databases
        firestore.get()
                .addOnSuccessListener {
                    partnerName = it["name"].toString()
                    partnerPhone = it["currentPhone"].toString()
                    binding.tvPartnerName.text = partnerName
                }

        val realtimeKey = combineUID(auth.uid, partnerUid) // Getting real time databases and messages
        realtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("$realtimeKey")

        realtime.child("0").setValue(RealtimeMessage(
            "System","Connected",auth.uid
        ))

        messageList = mutableListOf()
        setRV()

        realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<List<HashMap<String,String>>>()
                val newList = mutableListOf<SingleMessage>()
                for(a in value!!.indices){
                    if(a != 0){
                        val msg = value[a]["message"]
                        var owning = false

                        if(value[a]["fromUID"] == auth.uid){
                            owning = true
                        }

                        newList.add(SingleMessage(
                            owning,msg!!
                        ))
                    }
                }

                updateRV(newList)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity,"Failed to load messages!",Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnSendMessage.setOnClickListener {
            if(binding.etNewMessage.text.isNotEmpty()){
                val newMessage = binding.etNewMessage.text.toString()

                sendMessage(RealtimeMessage(
                        auth.displayName!!,newMessage,auth.uid
                ))

                sendNotification(
                        PushNotification(
                                NotificationData(auth.displayName!!, newMessage, auth.photoUrl.toString(), auth.uid),
                                partnerPhone
                        )
                )

                binding.etNewMessage.setText("")
            }
        }

        binding.btnGoBackToMessages.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setRV(){
        adapting = MessagingAdapter(messageList)

        binding.rvMessaging.adapter = adapting
        binding.rvMessaging.layoutManager = LinearLayoutManager(this)
        binding.rvMessaging.scrollToPosition(messageList.size-1)
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitObject.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.e("Sending","Sended?")
            } else {
                Log.e("Sending", "Error 1 : ${response.errorBody().toString()}")

            }
        } catch(e: Exception) {
            Log.e("Sending", "Error 2 : $e")
        }

    }

    private fun sendMessage(datas: RealtimeMessage){
        realtime.child((messageList.size+1).toString()).setValue(datas)
    }

    private fun updateRV(list: MutableList<SingleMessage>){
        messageList.clear()
        messageList.addAll(list)

        adapting.notifyDataSetChanged()
        binding.rvMessaging.scrollToPosition(messageList.size-1)

        if(messageList.isNotEmpty()){
            DatabaseFun.setup("last_messages")
            DatabaseFun.add("last_messages", partnerUid, messageList[messageList.size-1].message, messageList[messageList.size-1].isOwner)
        }


    }

    private fun combineUID(first: String, second: String): String?{

        for(a in first.indices){
            if(first[a].toInt() > second[a].toInt()){
                return (first+second)
            }
            else if(first[a].toInt() < second[a].toInt()){
                return (second+first)
            }
        }
        return first
    }
}