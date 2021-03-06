package com.yeocak.chatapp.ui.menu.fragments.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yeocak.chatapp.utils.LoginData
import com.yeocak.chatapp.databinding.FragmentAboutMeBinding

class AboutMeFragment : Fragment() {

    private lateinit var binding : FragmentAboutMeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutMeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUserId.text = "Your id: " + LoginData.userUID

        binding.btnGoGithub.setOnClickListener {
            val githubUrl = "https://github.com/yeocak/ChatApp"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(githubUrl)
            startActivity(intent)
        }

        binding.btnBackToSettings2.setOnClickListener {
            fragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
    }

}