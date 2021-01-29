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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.*
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentMessagesBinding
import java.text.SimpleDateFormat
import java.util.*

class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapting: MessagesAdapter
    private lateinit var messages : MutableList<SingleMessages>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messages = DatabaseFun.take("last_messages")

        adapting = MessagesAdapter(messages, (activity as MenuActivity))

        binding.rvMessages.adapter = adapting
        binding.rvMessages.layoutManager = LinearLayoutManager((activity as MenuActivity))


        val realtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference(userUID!!)

        realtime.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (a in dataSnapshot.children){
                    DatabaseFun.add("last_messages",a.key!!,a.child("last").value.toString(), a.child("date").value.toString())
                }
                Log.d("Heyt","Worked1")
                updateRV(DatabaseFun.take("last_messages"))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText((activity as MenuActivity),"Failed to load new messages!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRV(list: MutableList<SingleMessages>){
        messages.clear()
        messages.addAll(list)
        messages.sortByDescending { it.date }

        Log.d("Heyt",list.toString())

        adapting.notifyDataSetChanged()
    }
}