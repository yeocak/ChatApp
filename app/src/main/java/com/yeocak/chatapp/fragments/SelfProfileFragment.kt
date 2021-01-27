package com.yeocak.chatapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.FragmentSelfProfileBinding

class SelfProfileFragment : Fragment() {

    private lateinit var binding : FragmentSelfProfileBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelfProfileBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth.currentUser!!

        db.collection("detailedprofile").document(auth.uid).get().addOnSuccessListener {
            binding.etSelfProfileName.setText(auth.displayName)

            if(auth.photoUrl.toString() != "null"){
                binding.ibSelfProfileAvatar.load(auth.photoUrl)
            }

            if(it.data?.get("desc").toString() != "null"){
                binding.etSelfProfileExp.setText(it.data?.get("desc").toString())
            }

            if(it.data?.get("facebook").toString() != "null"){
                binding.etSelfProfileFacebook.setText(it.data?.get("facebook").toString())
                binding.cbSelfProfileFacebook.isChecked = true
            }

            if(it.data?.get("youtube").toString() != "null"){
                binding.etSelfProfileYoutube.setText(it.data?.get("youtube").toString())
                binding.cbSelfProfileYoutube.isChecked = true
            }

            if(it.data?.get("twitter").toString() != "null"){
                binding.etSelfProfileTwitter.setText(it.data?.get("twitter").toString())
                binding.cbSelfProfileTwitter.isChecked = true
            }

            if(it.data?.get("instagram").toString() != "null"){
                binding.etSelfProfileInstagram.setText(it.data?.get("instagram").toString())
                binding.cbSelfProfileInstagram.isChecked = true
            }
        }
    }

    override fun onStop() {
        super.onStop()

        val addingMap = hashMapOf<String, String>()
        val easyMap = hashMapOf<String,String>()

        addingMap["desc"] = binding.etSelfProfileExp.text.toString()

        if(!binding.etSelfProfileName.text.isNullOrEmpty()){
            val updating = userProfileChangeRequest {
                displayName = binding.etSelfProfileName.text.toString()
            }

            auth.updateProfile(updating).addOnSuccessListener {
                addingMap["name"] = binding.etSelfProfileName.text.toString()
                easyMap["name"] = binding.etSelfProfileName.text.toString()

            }
        }

        if(!binding.etSelfProfileInstagram.text.isNullOrEmpty() && binding.cbSelfProfileInstagram.isChecked){
            addingMap["instagram"] = binding.etSelfProfileInstagram.text.toString()
        }
        if(!binding.etSelfProfileFacebook.text.isNullOrEmpty() && binding.cbSelfProfileFacebook.isChecked){
            addingMap["facebook"] = binding.etSelfProfileFacebook.text.toString()
        }
        if(!binding.etSelfProfileTwitter.text.isNullOrEmpty() && binding.cbSelfProfileTwitter.isChecked){
            addingMap["twitter"] = binding.etSelfProfileTwitter.text.toString()
        }
        if(!binding.etSelfProfileYoutube.text.isNullOrEmpty() && binding.cbSelfProfileYoutube.isChecked){
            addingMap["youtube"] = binding.etSelfProfileYoutube.text.toString()
        }

        db.collection("detailedprofile").document(auth.uid).set(addingMap)
        db.collection("profile").document(auth.uid).set(easyMap)
    }
}