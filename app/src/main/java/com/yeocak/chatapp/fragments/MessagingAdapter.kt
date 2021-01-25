package com.yeocak.chatapp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yeocak.chatapp.R
import com.yeocak.chatapp.SingleMessage
import com.yeocak.chatapp.databinding.SingleMessageBinding

class MessagingAdapter(
    private val messageList : MutableList<SingleMessage>
) : RecyclerView.Adapter<MessagingAdapter.MessagingViewHolder>() {

    class MessagingViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = SingleMessageBinding.bind(view)
    }

    override fun getItemId(position: Int) = position.toLong()
    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagingViewHolder {
        return MessagingViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_message,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessagingViewHolder, position: Int) {
        val current = messageList[position]

        holder.binding.apply {
            if(current.isOwner){
                tvSelfMessage.visibility = VISIBLE
                tvSelfMessage.text = current.message
            }
            else{
                tvPartnerMessage.visibility = VISIBLE
                tvPartnerMessage.text = current.message
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}