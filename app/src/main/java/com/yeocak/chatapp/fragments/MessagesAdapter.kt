package com.yeocak.chatapp.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.ImageConvert
import com.yeocak.chatapp.R
import com.yeocak.chatapp.SingleMessages
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.database.DatabaseFun.getProfile
import com.yeocak.chatapp.database.LastMessage
import com.yeocak.chatapp.database.Photo
import com.yeocak.chatapp.database.Profile
import com.yeocak.chatapp.databinding.SingleMessagesMenuBinding
import kotlinx.coroutines.*

class MessagesAdapter(
        private val messagesList: MutableList<LastMessage>, private val context: Context
) : RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>() {

    class MessagesViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = SingleMessagesMenuBinding.bind(view)
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        return MessagesViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.single_messages_menu,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val current = messagesList[position]

        val fsdb = FirebaseFirestore.getInstance().collection("detailedprofile").document(current.uid)

        with(holder){

            var prof: Profile? = null

            GlobalScope.launch {
                val result = current.uid.getProfile(context)

                Log.d("Testing1", "1 $result")

                if(result == "download"){
                    fsdb.get().addOnSuccessListener { data ->
                        val newProf = Profile(
                                current.uid,
                                data["name"].toString(),
                                data["intro"].toString(),
                                data["facebook"].toString(),
                                data["youtube"].toString(),
                                data["twitter"].toString(),
                                data["instagram"].toString(),
                                data["version"].toString()
                        )

                        DatabaseFun.addProfile(newProf)

                        MainScope().launch {
                            val bitmap = ImageConvert.downloadImageBitmap(data["photo"].toString(),context)
                            val string = ImageConvert.getImageString(bitmap)

                            Log.d("Testing1", "1 $string")

                            if(string != null){
                                DatabaseFun.addPhoto(Photo(
                                        current.uid,
                                        string
                                ))
                            }

                            delay(1000)

                            val takePhoto = DatabaseFun.takePhoto(current.uid)
                            if(takePhoto != null){
                                if(takePhoto.photo.isNotEmpty()){
                                    val photo = ImageConvert.getBitmap(takePhoto.photo)
                                    binding.ivPersonPhoto.load(photo)
                                }
                            }
                        }
                        prof = DatabaseFun.takeProfile(current.uid)
                        binding.tvPersonName.text = prof!!.name
                    }
                }
                else if(result == "nothing"){
                    prof = DatabaseFun.takeProfile(current.uid)
                    binding.tvPersonName.text = prof!!.name

                    val takePhoto = DatabaseFun.takePhoto(current.uid)
                    if(takePhoto != null){
                        if(takePhoto.photo.isNotEmpty()){
                            val photo = ImageConvert.getBitmap(takePhoto.photo)
                            binding.ivPersonPhoto.load(photo)
                        }
                        else{
                            binding.ivPersonPhoto.load(R.drawable.ic_baseline_person_24)
                        }
                    }
                }
            }

            binding.tvPersonMessage.text = current.message

            binding.layoutBlock.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("uid",current.uid)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

}