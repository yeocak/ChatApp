package com.yeocak.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yeocak.chatapp.databinding.SingleMessagesMenuBinding

class MessagesAdapter(
        private val messagesList: MutableList<SingleMessages>
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
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

}