package com.yeocak.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.database.DatabaseFun.getProfile
import com.yeocak.chatapp.database.Message
import com.yeocak.chatapp.database.Profile
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import com.yeocak.chatapp.notification.NotificationData
import com.yeocak.chatapp.notification.PushNotification
import com.yeocak.chatapp.notification.RetrofitObject
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var adapting : MessagingAdapter
    private lateinit var messageList : MutableList<Message>
    private lateinit var auth : FirebaseUser

    private var partnerProfile: Profile? = null

    private lateinit var partnerPhone : String

    private lateinit var partnerUid: String

    private lateinit var rtdb : DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoginData.inMessages = true

        auth = Firebase.auth.currentUser!! // User auth
        partnerUid = intent.getStringExtra("uid")!! // Partner's uid

        setRV()

        MainScope().launch {
            partnerProfile = DatabaseFun.takeProfile(partnerUid)
            binding.tvPartnerName.text = partnerProfile?.name

            val takePhoto = DatabaseFun.takePhoto(partnerUid)
            if(takePhoto != null){
                if(takePhoto.photo.isNotEmpty()){
                    val photo = ImageConvert.getBitmap(takePhoto.photo)
                    binding.ivPartnerPhoto.load(photo)
                }
            }
        }

        rtdb = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("person_message")
                .child(userUID!!).child(partnerUid)

        rtdb.child("System").setValue("true")

        val takedList = mutableListOf<String>()

        rtdb.orderByChild("date").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (a in dataSnapshot.children) {
                    if (a.key!! != "System" && a.child("fromId").value.toString() == "Done" && !takedList.contains(a.key!!)) {
                        val newMessage = Message(
                                a.key!!,
                                partnerUid,
                                a.child("message").value.toString(),
                                a.child("photo").value.toString(),
                                a.child("date").value.toString(),
                                false
                        )
                        takedList.add(a.key!!)
                        DatabaseFun.addMessage(newMessage).also {
                            rtdb.child(a.key!!).apply {
                                child("fromId").removeValue()
                                child("date").removeValue()
                                child("message").removeValue()
                                child("photo").removeValue()
                            }
                        }
                    }
                }

                updateRV(DatabaseFun.takeMessage(partnerUid))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessageActivity,"Failed to load new messages!", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnSendMessage.setOnClickListener {
            if(binding.etNewMessage.text.isNotEmpty()){
                val messageText = binding.etNewMessage.text.toString()

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
                            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                            val currentDate = sdf.format(Date())

                            val uniqNumb = UUID.randomUUID()

                            sendMessage(Message(
                                    uniqNumb.toString(),
                                    partnerUid,
                                    messageText,
                                    null,
                                    currentDate,
                                    true
                            ))

                            binding.etNewMessage.setText("")
                        }
                        else{
                            Toast.makeText(this, "You can't Message to this user anymore!", Toast.LENGTH_SHORT).show()
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
        messageList = DatabaseFun.takeMessage(partnerUid)

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

    private fun sendMessage(datas: Message){
        val partnerInbox = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("person_message")
                .child(partnerUid).child(userUID!!)

        val lastPartner = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("last_message")
                .child(partnerUid).child(userUID!!)
        val lastUser = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("last_message")
                .child(userUID!!).child(partnerUid)

        val maping = hashMapOf(
                "fromId" to datas.fromId,
                "message" to datas.message,
                "photo" to datas.photo,
                "date" to datas.date
        )

        partnerInbox.child(datas.uniq).setValue(maping).addOnSuccessListener {
            partnerInbox.child(datas.uniq).child("fromId").setValue("Done").addOnSuccessListener {
                lastPartner.child("message").setValue(datas.message)
                lastPartner.child("date").setValue(datas.date)

                lastUser.child("message").setValue(datas.message)
                lastUser.child("date").setValue(datas.date)

                DatabaseFun.addMessage(datas)
                updateRV(DatabaseFun.takeMessage(partnerUid))
            }
        }

        FirebaseFirestore.getInstance().collection("profile").document(partnerUid).get().addOnSuccessListener {
            sendNotification(
                    PushNotification(
                            NotificationData(auth.displayName!!, datas.message, auth.photoUrl.toString(), userUID!!),
                            it["phone"].toString()
                    )
            )
        }

    }

    private fun updateRV(list: MutableList<Message>){
        messageList.clear()
        messageList.addAll(list)

        adapting.notifyDataSetChanged()
        binding.rvMessaging.scrollToPosition(messageList.size - 1)
    }


    override fun onStop() {
        super.onStop()
        LoginData.inMessages = false
    }
}