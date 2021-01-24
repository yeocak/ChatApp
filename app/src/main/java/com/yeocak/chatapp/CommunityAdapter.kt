package com.yeocak.chatapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.databinding.SingleCommunityBlockBinding

class CommunityAdapter(
        private val communityList: MutableList<SingleCommunity>, private val context: Context
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    class CommunityViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = SingleCommunityBlockBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        return CommunityViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.single_community_block,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val current = communityList[position]

        with(holder){
            binding.ivCommunityName.text = current.name

            binding.layoutCommunity.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("uid",current.uid)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

}