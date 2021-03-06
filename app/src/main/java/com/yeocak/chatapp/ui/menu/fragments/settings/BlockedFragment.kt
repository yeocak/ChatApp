package com.yeocak.chatapp.ui.menu.fragments.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.ui.menu.MenuActivity
import com.yeocak.chatapp.databinding.FragmentBlockedBinding

class BlockedFragment : Fragment() {

    private lateinit var binding : FragmentBlockedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentBlockedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val blockedListName = mutableListOf<String>()
        val blockedListId = mutableListOf<String>()

        val adapting = BlockedAdapter(blockedListName, blockedListId)

        binding.rvBlocked.adapter = adapting
        binding.rvBlocked.layoutManager = LinearLayoutManager(activity as MenuActivity)

        db.collection("block").document(userUID!!).collection("to").get().addOnSuccessListener { blockedTo ->
            db.collection("profile").document(userUID!!).get().addOnSuccessListener { profile ->
                for(a in blockedTo){
                    if(a.id != "system"){
                        if(a.get("is").toString() == "true"){

                            blockedListName.add(profile["name"].toString())
                            blockedListId.add(a.id)

                        }
                    }
                }
                adapting.notifyDataSetChanged()
            }

        }

        binding.btnBackToSettings.setOnClickListener {
            fragmentManager?.beginTransaction()?.remove(this)?.commit()
        }



    }

}