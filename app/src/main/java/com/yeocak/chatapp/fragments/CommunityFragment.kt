package com.yeocak.chatapp.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentCommunityBinding
import kotlinx.coroutines.*
import java.util.*

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var Fdb : FirebaseFirestore
    private lateinit var communityList : MutableList<SingleCommunity>
    private lateinit var displayList : MutableList<SingleCommunity>
    private lateinit var blockList : MutableSet<String>
    private lateinit var adapting : CommunityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fdb = FirebaseFirestore.getInstance()

        transferingFM = this.fragmentManager
        transferingActivity = (activity as MenuActivity)

        communityList = mutableListOf()
        blockList = mutableSetOf()

        try {
            GlobalScope.launch{
                Fdb.collection("profile").get().addOnSuccessListener { allUsers ->

                    Fdb.collection("block").document(userUID!!).collection("from").get().addOnSuccessListener { from ->
                        Fdb.collection("block").document(userUID!!).collection("to").get().addOnSuccessListener { to ->
                            for(a in from){
                                if(a["is"] == true){
                                    blockList.add(a.id)
                                }
                            }
                            for(a in to){
                                if(a["is"] == true){
                                    blockList.add(a.id)
                                }
                            }
                            Log.d("Heyo",blockList.toString())
                            for (document in allUsers) {
                                if(document.id != userUID!! && !blockList.contains(document.id)){
                                    val new = SingleCommunity(document.data["name"].toString(), document.id,document.data["photo"].toString())
                                    communityList.add(new)
                                }
                            }
                            displayList = mutableListOf()
                            displayList.addAll(communityList)
                            createRecycler()
                        }
                    }

                }.addOnCanceledListener {
                    Log.d("Heyt","Worked2")
                }.addOnFailureListener {
                    Log.d("Heyt","Worked3")
                }
            }
        }catch (e: Exception){
            Toast.makeText((activity as MenuActivity), "Something went wrong!", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.svCommunity.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val newList = mutableListOf<SingleCommunity>()

                    for(a in communityList) {
                        if (a.name.toLowerCase(Locale.ROOT).contains(query!!.toLowerCase(Locale.ROOT))) {
                            newList.add(a)
                        }
                    }

                    displayList.clear()
                    displayList.addAll(newList)

                    adapting.notifyDataSetChanged()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        binding.svCommunity.setOnCloseListener {
            displayList.clear()
            displayList.addAll(communityList)

            adapting.notifyDataSetChanged()
            true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun createRecycler(){
        adapting = CommunityAdapter(
                displayList, transferingFM!!
        )

        val row = (transferinWidth!! / 120).toInt()

        binding.rvCommunity.adapter = adapting
        binding.rvCommunity.layoutManager = GridLayoutManager(transferingActivity,row)
    }

    companion object{
        var transferingFM : FragmentManager? = null
        var transferingActivity : Context? = null
        var transferinWidth : Float? = null
    }
}