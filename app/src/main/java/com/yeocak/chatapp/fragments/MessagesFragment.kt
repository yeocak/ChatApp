package com.yeocak.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.yeocak.chatapp.MessagesAdapter
import com.yeocak.chatapp.R
import com.yeocak.chatapp.SingleMessages
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapting = MessagesAdapter(mutableListOf<SingleMessages>(
                SingleMessages("Emre","Bu bir deneme mesajı"),
                SingleMessages("Ahmet","Bu bir deneme mesajı2"),
                SingleMessages("Mehmet","Bu bir deneme mesajı3"),
                SingleMessages("Emre2","Bu bir deneme mesajı4"),
                SingleMessages("Emre345","Bu bir deneme mesajı5"),
                SingleMessages("Emre546","Bu bir deneme mesajı6"),
                SingleMessages("Emr123e","Bu bir deneme mesajı7"),
                SingleMessages("Emre345","Bu bir deneme mesajı8"),
                SingleMessages("Emr567","Bu bir deneme mesajı9"),
                SingleMessages("Emr321e","Bu bir deneme mesajı10"),
        ))

        binding.rvMessages.adapter = adapting
        binding.rvMessages.layoutManager = LinearLayoutManager((activity as MenuActivity))
    }
}