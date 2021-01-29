package com.yeocak.chatapp.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.activities.LoginActivity
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = (activity as MenuActivity).getSharedPreferences(
                "notification${userUID}", Context.MODE_PRIVATE)
        binding.sNotification.isChecked = sharedPref!!.getBoolean("notification${userUID}", true)

        binding.btnSignOut.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent((activity as MenuActivity),LoginActivity::class.java)
            startActivity(intent)
            (activity as MenuActivity).finishAffinity()
        }

        binding.btnGithub.setOnClickListener {
            val githubUrl = "https://github.com/yeocak/ChatApp"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(githubUrl)
            startActivity(intent)
        }

        binding.sNotification.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()){
                putBoolean("notification${userUID}", isChecked)
                apply()
            }

        }
    }
}