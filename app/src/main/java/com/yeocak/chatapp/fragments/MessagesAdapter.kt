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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.ImageConvert
import com.yeocak.chatapp.R
import com.yeocak.chatapp.SingleMessages
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.databinding.SingleMessagesMenuBinding

class MessagesAdapter(
        private val messagesList: MutableList<SingleMessages>, private val context: Context
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

        val photo = ImageConvert.getBitmap(current.photo)
        Log.d("img", "Ops: $photo")

        with(holder){

            binding.tvPersonName.text = current.name
            binding.tvPersonMessage.text = current.lastMessage
            if(photo.toString() != "null"){
                binding.ivPersonPhoto.load(photo)
            }
            else{
                binding.ivPersonPhoto.load(R.drawable.ic_baseline_person_24)
            }


            binding.layoutBlock.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("uid",current.uid)
                intent.putExtra("photo",current.photo)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

}