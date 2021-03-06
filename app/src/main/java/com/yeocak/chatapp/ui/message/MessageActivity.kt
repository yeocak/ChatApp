package com.yeocak.chatapp.ui.message

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yeocak.chatapp.*
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.model.Message
import com.yeocak.chatapp.model.Photo
import com.yeocak.chatapp.model.Profile
import com.yeocak.chatapp.databinding.ActivityMessageBinding
import com.yeocak.chatapp.ui.menu.fragments.CommunityAdapter
import com.yeocak.chatapp.model.NotificationData
import com.yeocak.chatapp.model.PushNotification
import com.yeocak.chatapp.notification.RetrofitObject
import com.yeocak.chatapp.ui.menu.MenuActivity
import com.yeocak.chatapp.utils.ImageConvert
import com.yeocak.chatapp.utils.LoginData
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var adapting : MessagingAdapter
    private lateinit var messageList : MutableList<Message>
    private lateinit var auth : FirebaseUser

    private var partnerProfile: Profile? = null

    private lateinit var partnerUid: String

    private lateinit var rtdb : DatabaseReference

    private var sendImageUri: Uri? = null

    private var messageLimit = 20


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

                        if (a.child("photo").value.toString() != "null") {
                            downloadImage(a.child("photo").value.toString())
                        }
                    }
                }

                updateRV(DatabaseFun.takeMessage(partnerUid, messageLimit))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CustomError", "35764")
            }
        })

        binding.btnSendMessage.setOnClickListener {
            if(binding.etNewMessage.text.isNotEmpty()){
                val messageText = binding.etNewMessage.text.toString()

                val blockList = mutableSetOf<String>()
                val fdb = FirebaseFirestore.getInstance()

                fdb.collection("block").document(userUID!!).collection("from").get().addOnSuccessListener { from ->
                    fdb.collection("block").document(userUID!!).collection("to").get().addOnSuccessListener { to ->
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

                            val newMes = Message(
                                    uniqNumb.toString(),
                                    partnerUid,
                                    messageText,
                                    null,
                                    currentDate,
                                    true
                            )

                            if(sendImageUri != null){
                                binding.pbLoadingPhoto.visibility = VISIBLE
                                binding.etNewMessage.isEnabled = false
                                binding.btnSendMessage.isEnabled = false
                                loadPhoto(newMes)
                            }
                            else{
                                sendMessage(newMes)
                            }
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

        binding.rvMessaging.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.rvMessaging.smoothScrollToPosition(adapting.itemCount)
            }
        }

        binding.btnAddPhotoMessage.setOnClickListener {

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 101)

        }

        binding.btnCancelPhoto.setOnClickListener {
            binding.layoutLoadingPhoto.visibility = GONE
            sendImageUri = null
        }

        binding.layoutRefresh.setOnRefreshListener {
            messageLimit += 20
            updateRV(DatabaseFun.takeMessage(partnerUid, messageLimit), false)
            binding.layoutRefresh.isRefreshing = false
        }

        binding.tvPartnerName.setOnClickListener {
            val action = Intent(this, MenuActivity::class.java)
            action.putExtra("fragment","profiles")
            CommunityAdapter.transferprofileUid = partnerUid
            startActivity(action)
            finishAffinity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 101) {
            sendImageUri = data?.data

            if(sendImageUri != null){
                binding.layoutLoadingPhoto.visibility = VISIBLE
                binding.ivLoadingPhoto.load(sendImageUri)
            }
        }
    }

    private fun loadPhoto(data: Message){
        val ref = Firebase.storage.reference.child("message_photos/${data.uniq}.jpg")

        val bitmapTo = MediaStore.Images.Media.getBitmap(contentResolver, sendImageUri).scale(1000, 1000, false)
        val baos = ByteArrayOutputStream()
        bitmapTo.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byted: ByteArray = baos.toByteArray()

            ref.putBytes(byted).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { Uring ->

                    sendMessage(
                            Message(
                                    data.uniq,
                                    data.fromId,
                                    data.message,
                                    Uring.toString(),
                                    data.date,
                                    data.isOwner
                            )
                    )
                }
            }.addOnCompleteListener {
                binding.pbLoadingPhoto.visibility = GONE
                binding.etNewMessage.isEnabled = true
                binding.btnSendMessage.isEnabled = true
            }
    }

    private fun setRV(){
        messageList = DatabaseFun.takeMessage(partnerUid, messageLimit)

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

                binding.etNewMessage.setText("")
                binding.layoutLoadingPhoto.visibility = GONE

                DatabaseFun.addMessage(datas)

                if(datas.photo.toString() != "null"){
                    MainScope().launch {

                        val bitmapTo = MediaStore.Images.Media.getBitmap(contentResolver, sendImageUri).scale(1000, 1000, false)

                        val stringTo = ImageConvert.getImageString(bitmapTo)
                        if(stringTo != null){
                            DatabaseFun.addPhoto(
                                    Photo(
                                            datas.photo.toString(),
                                            stringTo
                                    )
                            )
                            updateRV(DatabaseFun.takeMessage(partnerUid, messageLimit))
                            sendImageUri = null
                        }
                    }
                }

                updateRV(DatabaseFun.takeMessage(partnerUid, messageLimit))
            }
        }

        FirebaseFirestore.getInstance().collection("profile").document(partnerUid).get().addOnSuccessListener {
            Log.d("MessagingNot", NotificationData(auth.displayName!!, datas.message, auth.photoUrl.toString(), userUID!!).toString() + it["currentPhone"].toString())
            sendNotification(
                    PushNotification(
                            NotificationData(auth.displayName!!, datas.message, auth.photoUrl.toString(), userUID!!),
                            it["currentPhone"].toString()
                    )
            )
        }

    }

    private fun updateRV(list: MutableList<Message>, scroll: Boolean = true){
        messageList.clear()
        messageList.addAll(list)
        messageList.sortBy {
                SimpleDateFormat("dd/M/yyyy hh:mm:ss").parse(it.date)
        }

        adapting.notifyDataSetChanged()
        if(scroll){
            binding.rvMessaging.scrollToPosition(messageList.size - 1)
        }
        else{
            binding.rvMessaging.scrollToPosition(22)
        }

    }

    private fun downloadImage(imageUrl: String){
        MainScope().launch {
            val bitmapTo = ImageConvert.downloadImageBitmap(imageUrl, this@MessageActivity, 1000, 1000)
            val stringTo = ImageConvert.getImageString(bitmapTo)
            if(stringTo != null){
                DatabaseFun.addPhoto(
                        Photo(
                                imageUrl,
                                stringTo
                        )
                )
                updateRV(DatabaseFun.takeMessage(partnerUid, messageLimit))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        LoginData.inMessages = false
    }

}