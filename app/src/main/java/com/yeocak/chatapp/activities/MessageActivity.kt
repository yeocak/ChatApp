package com.yeocak.chatapp.activities

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import com.yeocak.chatapp.notification.NotificationData
import com.yeocak.chatapp.notification.PushNotification
import com.yeocak.chatapp.notification.RetrofitObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var adapting : MessagingAdapter
    private lateinit var messageList : MutableList<SingleMessage>
    private lateinit var auth : FirebaseUser

    private lateinit var partnerName : String
    private lateinit var partnerPhone : String

    private lateinit var partnerUid: String

    private lateinit var realtime : DatabaseReference
    private lateinit var partnerRealtime : DatabaseReference
    private lateinit var myRealtime: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoginData.inMessages = true

        auth = Firebase.auth.currentUser!! // User auth
        partnerUid = intent.getStringExtra("uid")!! // Partner's uid
        if(!intent.getStringExtra("photo").isNullOrEmpty()){
            val partnerPhoto = ImageConvert.getBitmap(intent.getStringExtra("photo"))
            binding.ivPartnerPhoto.load(partnerPhoto)
        }
        else{
            binding.ivPartnerPhoto.load(R.drawable.ic_baseline_person_24)
        }

        val firestore = FirebaseFirestore.getInstance().collection("profile").document(partnerUid) // Getting profile databases
        firestore.get()
                .addOnSuccessListener {
                    partnerName = it["name"].toString()
                    partnerPhone = it["currentPhone"].toString()
                    binding.tvPartnerName.text = partnerName
                }

        val realtimeKey = combineUID(auth.uid, partnerUid) // Getting real time databases and messages
        realtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("$realtimeKey")
        partnerRealtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference(
                partnerUid).child(userUID!!)
        myRealtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference(
                userUID!!).child(partnerUid)

        realtime.child("0").setValue(RealtimeMessage(
                "System", "Connected", auth.uid
        ))

        messageList = mutableListOf()
        setRV()

        realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<List<HashMap<String, String>>>()
                val newList = mutableListOf<SingleMessage>()
                for (a in value!!.indices) {
                    if (a != 0) {
                        val msg = value[a]["message"]
                        var owning = false

                        if (value[a]["fromUID"] == auth.uid) {
                            owning = true
                        }

                        newList.add(SingleMessage(
                                owning, msg!!
                        ))
                    }
                }

                updateRV(newList)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity, "Failed to load messages!", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnSendMessage.setOnClickListener {
            if(binding.etNewMessage.text.isNotEmpty()){
                val newMessage = binding.etNewMessage.text.toString()

                val blockList = mutableSetOf<String>()
                val fdb = FirebaseFirestore.getInstance()

                fdb.collection("block").document(LoginData.userUID!!).collection("from").get().addOnSuccessListener { from ->
                    fdb.collection("block").document(LoginData.userUID!!).collection("to").get().addOnSuccessListener { to ->
                        for(a in from){
                            if(a["is"] == true){
                                blockList.add(a.id)
                            }
                        }
                        for(a in to){
                            if(a["is"] == true){
                                blockList.add(a.id)
                            }
                        }
                        if(!blockList.contains(partnerUid)){
                            sendMessage(RealtimeMessage(
                                    auth.displayName!!, newMessage, auth.uid
                            ))

                            sendNotification(
                                    PushNotification(
                                            NotificationData(auth.displayName!!, newMessage, auth.photoUrl.toString(), auth.uid),
                                            partnerPhone
                                    )
                            )

                            binding.etNewMessage.setText("")
                        }
                        else{
                            Toast.makeText(this, "You can't message to this user anymore!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.btnGoBackToMessages.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        binding.rvMessaging.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvMessaging.smoothScrollToPosition(adapting.itemCount)
            }
        }

    }



    private fun setRV(){
        adapting = MessagingAdapter(messageList)

        binding.rvMessaging.adapter = adapting
        binding.rvMessaging.layoutManager = LinearLayoutManager(this)
        binding.rvMessaging.scrollToPosition(messageList.size - 1)
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitObject.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.e("Sending", "Sended")
            } else {
                Log.e("Sending", "Error 1 : ${response.errorBody().toString()}")

            }
        } catch (e: Exception) {
            Log.e("Sending", "Error 2 : $e")
        }

    }

    private fun sendMessage(datas: RealtimeMessage){
        realtime.child((messageList.size + 1).toString()).setValue(datas)
    }

    private fun updateRV(list: MutableList<SingleMessage>){
        messageList.clear()
        messageList.addAll(list)

        adapting.notifyDataSetChanged()
        binding.rvMessaging.scrollToPosition(messageList.size - 1)

        if(messageList.isNotEmpty()){
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val todayDate = dateFormat.format(Date()).toString()

            partnerRealtime.child("last").setValue(messageList[messageList.size - 1].message)
            partnerRealtime.child("date").setValue(todayDate)

            myRealtime.child("last").setValue(messageList[messageList.size - 1].message)
            myRealtime.child("date").setValue(todayDate)
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
        return (first+second)
    }

    override fun onStop() {
        super.onStop()
        LoginData.inMessages = false
    }
}