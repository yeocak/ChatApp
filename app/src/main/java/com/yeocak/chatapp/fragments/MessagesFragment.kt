package com.yeocak.chatapp.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
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
import com.yeocak.chatapp.databinding.FragmentMessagesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

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
            messages.sortByDescending { it.date }

            adapting = MessagesAdapter(messages, (activity as MenuActivity))

            binding.rvMessages.adapter = adapting
            binding.rvMessages.layoutManager = LinearLayoutManager((activity as MenuActivity))


            val realtime = Firebase.database("https://chatapp-35faa-default-rtdb.europe-west1.firebasedatabase.app/").getReference(userUID!!)

            val db = FirebaseFirestore.getInstance()


            realtime.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (a in dataSnapshot.children){

                        db.collection("profile").document(a.key!!).get(Source.SERVER).addOnSuccessListener {

                            GlobalScope.launch {

                                    val photoBits = ImageConvert.downloadImageBitmap(it["photo"].toString(),(activity as MenuActivity))
                                    val photoString = ImageConvert.getImageString(photoBits)

                                    DatabaseFun.add("last_messages",
                                                a.key!!,
                                                a.child("last").value.toString(),
                                                it["name"].toString(),
                                                photoString,
                                                a.child("date").value.toString())

                                    withContext(Main){
                                        updateRV(DatabaseFun.take("last_messages"))
                                    }

                            }

                        }
                    }

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

        adapting.notifyDataSetChanged()
    }
}