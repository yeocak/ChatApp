package com.yeocak.chatapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import coil.load
import com.yeocak.chatapp.DatabaseFun
import com.yeocak.chatapp.R
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.activities.MessageActivity
import com.yeocak.chatapp.databinding.FragmentProfilesBinding
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileAvatar
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileDesc
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileFacebook
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileInstagram
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileName
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileTwitter
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileUid
import com.yeocak.chatapp.fragments.CommunityAdapter.Companion.transferprofileYoutube

class ProfilesFragment : Fragment() {

    private lateinit var binding : FragmentProfilesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvProfileName.text = transferprofileName
        if(transferprofileDesc != "null") {
            binding.tvProfileExp.text = transferprofileDesc
        }
        if(transferprofileAvatar != "null"){
            binding.ivProfileAvatar.load(transferprofileAvatar)
        }
        if(transferprofileFacebook != "null"){
            binding.layoutProfileFacebook.visibility = VISIBLE
            binding.tvProfileFacebook.text = transferprofileFacebook
        }
        if(transferprofileTwitter != "null"){
            binding.layoutProfileTwitter.visibility = VISIBLE
            binding.tvProfileTwitter.text = transferprofileTwitter
        }
        if(transferprofileYoutube != "null"){
            binding.layoutProfileYoutube.visibility = VISIBLE
            binding.tvProfileYoutube.text = transferprofileYoutube
        }
        if(transferprofileInstagram != "null"){
            binding.layoutProfileInstagram.visibility = VISIBLE
            binding.tvProfileInstagram.text = transferprofileInstagram
        }

        binding.btnProfileSendMessage.setOnClickListener {

            val intent = Intent((activity as MenuActivity), MessageActivity::class.java)
            intent.putExtra("uid", transferprofileUid)
            startActivity(intent)

        }

    }
}