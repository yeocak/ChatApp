package com.yeocak.chatapp.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentCommunityBinding
import kotlinx.coroutines.currentCoroutineContext

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var Fdb : FirebaseFirestore
    private lateinit var communityList : MutableList<SingleCommunity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fdb = FirebaseFirestore.getInstance()

        communityList = mutableListOf<SingleCommunity>()

        Fdb.collection("profile").get().addOnSuccessListener {
            for (document in it) {
                if(document.id != Firebase.auth.currentUser!!.uid){
                    val new = SingleCommunity(document.data["name"].toString(), document.id,document.data["photo"].toString())
                    communityList.add(new)
                }
            }
            createRecycler()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun screenWidth(): Float{
        val displayMetrics = DisplayMetrics()
        (activity as MenuActivity).windowManager?.defaultDisplay!!.getRealMetrics(displayMetrics)

        return displayMetrics.xdpi
    }

    private fun createRecycler(){
        val adapting = CommunityAdapter(
                communityList, this.fragmentManager!!
        )

        val row = (screenWidth() / 120).toInt()

        binding.rvCommunity.adapter = adapting
        binding.rvCommunity.layoutManager = GridLayoutManager((activity as MenuActivity),row)
    }
}