package com.yeocak.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yeocak.chatapp.databinding.SingleCommunityBlockBinding

class CommunityAdapter(
        private val communityList: MutableList<SingleCommunity>
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
        }
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

}