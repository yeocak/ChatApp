package com.yeocak.chatapp.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapting = CommunityAdapter(mutableListOf<SingleCommunity>(
                SingleCommunity("Emre"),
                SingleCommunity("Ahmet"),
                SingleCommunity("Mehmet"),
                SingleCommunity("Emre2"),
                SingleCommunity("Emre345"),
                SingleCommunity("Emre546"),
                SingleCommunity("Emr123e"),
                SingleCommunity("Emre345"),
                SingleCommunity("Emr567"),
                SingleCommunity("Tester")
        ))

        val row = (screenWidth() / 120).toInt()

        binding.rvCommunity.adapter = adapting
        binding.rvCommunity.layoutManager = GridLayoutManager((activity as MenuActivity),row)
    }

    private fun screenWidth(): Float{
        val displayMetrics = DisplayMetrics()
        (activity as MenuActivity).windowManager?.defaultDisplay!!.getRealMetrics(displayMetrics)
        return displayMetrics.xdpi
    }
}