package com.yeocak.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.activities.MenuActivity.Companion.menuActivity
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.database.LastMessage
import com.yeocak.chatapp.database.Message
import com.yeocak.chatapp.databinding.FragmentMessagesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapting: MessagesAdapter
    private lateinit var messages : MutableList<LastMessage>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(layoutInflater)

        messages = DatabaseFun.takeLasts()

        adapting = MessagesAdapter(messages, (activity as MenuActivity))

        binding.rvMessages.adapter = adapting
        binding.rvMessages.layoutManager = LinearLayoutManager((activity as MenuActivity))


        val rtdb = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference("last_message")
                .child(userUID!!)

        rtdb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for(i in dataSnapshot.children){
                        val newLast = LastMessage(
                                i.key!!,
                                i.child("message").value.toString(),
                                i.child("date").value.toString()
                                    )

                        DatabaseFun.addLast(newLast)
                    }

                updateRV(DatabaseFun.takeLasts())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("CustomError", "15342 ")
            }
        })


        return binding.root
    }

    private fun updateRV(list: MutableList<LastMessage>){
        messages.clear()
        messages.addAll(list)
        messages.sortByDescending {
            SimpleDateFormat("dd/M/yyyy hh:mm:ss").parse(it.date)
        }

        adapting.notifyDataSetChanged()
    }
}