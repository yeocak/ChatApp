package com.yeocak.chatapp.fragments.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.activities.LoginActivity
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var db : FirebaseFirestore

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = (activity as MenuActivity).getSharedPreferences(
                "notification${userUID}", Context.MODE_PRIVATE)
        binding.sNotification.isChecked = sharedPref!!.getBoolean("notification${userUID}", true)

        binding.btnSignOut.setOnClickListener {
            Firebase.auth.signOut()

            val intent = Intent((activity as MenuActivity),LoginActivity::class.java)
            startActivity(intent)
            (activity as MenuActivity).finishAffinity()
        }

        binding.sNotification.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()){
                putBoolean("notification${userUID}", isChecked)
                apply()
            }
        }
        
        binding.btnBlockedUsers.setOnClickListener {
            fragmentManager?.beginTransaction()?.replace(R.id.flTopSettings, BlockedFragment())?.commit()
        }

        binding.btnAboutMe.setOnClickListener {
            fragmentManager?.beginTransaction()?.replace(R.id.flTopSettings, AboutMeFragment())?.commit()
        }

        binding.btnDeleteAcc.setOnClickListener {
            // NOT DONE
            //deleteOther()
        }
    }

    private fun deleteOther(){
        Log.d("Heyt","0")
        db.collection("block").document(userUID!!).collection("from").get().addOnSuccessListener {
            Log.d("Heyt","1")
            for(a in it){
                Log.d("Heyt","2")
                if(a.id != "system"){
                    Log.d("Heyt","3")
                    db.collection("block").document(a.id).collection("to").document(userUID!!).delete().addOnSuccessListener {
                        Log.d("Heyt","4")
                    }
                }
            }
            deleteOwn()
        }.addOnFailureListener {
            Toast.makeText((activity as MenuActivity), "Connection issue!",Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText((activity as MenuActivity), "Connection issue!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteOwn(){
        Log.d("Heyt","5")
        db.collection("profile").document(userUID!!).delete().addOnSuccessListener {
            Log.d("Heyt","6")
            db.collection("detailedprofile").document(userUID!!).delete().addOnSuccessListener {
                Log.d("Heyt","7")
                db.collection("block").document(userUID!!).delete().addOnSuccessListener {
                    Log.d("Heyt","8")
                    Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                        Log.d("Heyt","9")
                        val intent = Intent((activity as MenuActivity), LoginActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener {
                        Log.d("Heyt","${it.toString()}, ${it.printStackTrace()}, ${it.localizedMessage}, ${it.message}, ${it}")
                    }.addOnCanceledListener {
                        Log.d("Heyt","Canceled")
                    }
                }
            }
        }
    }
}