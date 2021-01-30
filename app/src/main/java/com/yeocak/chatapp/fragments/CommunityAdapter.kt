package com.yeocak.chatapp.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.R
import com.yeocak.chatapp.SingleCommunity
import com.yeocak.chatapp.databinding.SingleCommunityBlockBinding

class CommunityAdapter(
        private val communityList: MutableList<SingleCommunity>, private val fm : FragmentManager
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

            if(current.photo != "null"){
                binding.ivCommunityImage.load(current.photo)
            }
            else{
                binding.ivCommunityImage.load(R.drawable.ic_baseline_person_24)
            }


            binding.layoutCommunity.setOnClickListener {
                FirebaseFirestore.getInstance().collection("detailedprofile").document(current.uid).get().addOnSuccessListener {

                    transferprofileName = it.data?.get("name").toString()
                    transferprofileDesc= it.data?.get("desc").toString()
                    transferprofileAvatar= it.data?.get("photo").toString()
                    transferprofileFacebook= it.data?.get("facebook").toString()
                    transferprofileTwitter= it.data?.get("twitter").toString()
                    transferprofileInstagram= it.data?.get("instagram").toString()
                    transferprofileYoutube= it.data?.get("youtube").toString()
                    transferprofileUid= current.uid

                    fm.beginTransaction().replace(R.id.frMain, ProfilesFragment()).commit()
                }
            }
        }
    }

    companion object{
        // Transfer data between fragments

        var transferprofileName: String? = null
        var transferprofileDesc: String? = null
        var transferprofileAvatar: String? = null
        var transferprofileFacebook: String? = null
        var transferprofileTwitter: String? = null
        var transferprofileInstagram: String? = null
        var transferprofileYoutube: String? = null
        var transferprofileUid: String? = null
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

}