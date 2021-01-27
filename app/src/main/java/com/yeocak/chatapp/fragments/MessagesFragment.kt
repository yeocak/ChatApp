package com.yeocak.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.yeocak.chatapp.*
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messages = DatabaseFun.take("last_messages")

        val adapting = MessagesAdapter(messages, (activity as MenuActivity))

        binding.rvMessages.adapter = adapting
        binding.rvMessages.layoutManager = LinearLayoutManager((activity as MenuActivity))

    }
}