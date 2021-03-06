package com.yeocak.chatapp.ui.message

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.yeocak.chatapp.utils.ImageConvert
import com.yeocak.chatapp.R
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.model.Message
import com.yeocak.chatapp.databinding.SingleMessageBinding

class MessagingAdapter(
    private val messageList : MutableList<Message>
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
                layoutOwner.visibility = VISIBLE
                layoutPartning.visibility = GONE
                tvSelfMessage.text = current.message

                try {
                    if(current.photo.toString() != "null"){

                        val photoString = DatabaseFun.takePhoto(current.photo.toString())?.photo

                        if(photoString.toString() != "null"){
                            val photoBit = ImageConvert.getBitmap(photoString)
                            ivMessagePhotoOwner.visibility = VISIBLE
                            ivMessagePhotoOwner.load(photoBit)
                        }
                    }
                    else{
                        ivMessagePhotoOwner.visibility = GONE
                    }
                }catch (e: Exception){
                    Log.d("Error Adapter", "Error No: 9386")
                }
            }
            else{
                layoutOwner.visibility = GONE
                layoutPartning.visibility = VISIBLE
                tvPartnerMessage.text = current.message

                try {
                    if(current.photo.toString() != "null"){

                        val photoString = DatabaseFun.takePhoto(current.photo.toString())?.photo

                        if(photoString.toString() != "null"){
                            val photoBit = ImageConvert.getBitmap(photoString)
                            ivMessagePhotoPartner.visibility = VISIBLE
                            ivMessagePhotoPartner.load(photoBit)
                        }
                    }
                    else{
                        ivMessagePhotoPartner.visibility = GONE
                    }
                }catch (e: Exception){
                    Log.d("Error Adapter", "Error No: 9387")
                }


            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}