package com.yeocak.chatapp.fragments

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

        with(holder){
            binding.tvPersonName.text = current.name
            binding.tvPersonMessage.text = current.lastMessage

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