package com.yeocak.chatapp.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.ktx.storage
import com.yeocak.chatapp.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.activities.MenuActivity
import com.yeocak.chatapp.databinding.FragmentSelfProfileBinding
import java.io.File

class SelfProfileFragment : Fragment() {

    private lateinit var binding: FragmentSelfProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseUser

    private var imageUri: Uri? = null


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

        Log.d("Heyy", auth.photoUrl.toString())

        db.collection("detailedprofile").document(auth.uid).get().addOnSuccessListener {
            binding.etSelfProfileName.setText(auth.displayName)

            if (auth.photoUrl.toString() != "null") {
                binding.ibSelfProfileAvatar.load(auth.photoUrl)
            }

            if (it.data?.get("desc").toString() != "null") {
                binding.etSelfProfileExp.setText(it.data?.get("desc").toString())
            }

            if (it.data?.get("facebook").toString() != "null") {
                binding.etSelfProfileFacebook.setText(it.data?.get("facebook").toString())
                binding.cbSelfProfileFacebook.isChecked = true
            }

            if (it.data?.get("youtube").toString() != "null") {
                binding.etSelfProfileYoutube.setText(it.data?.get("youtube").toString())
                binding.cbSelfProfileYoutube.isChecked = true
            }

            if (it.data?.get("twitter").toString() != "null") {
                binding.etSelfProfileTwitter.setText(it.data?.get("twitter").toString())
                binding.cbSelfProfileTwitter.isChecked = true
            }

            if (it.data?.get("instagram").toString() != "null") {
                binding.etSelfProfileInstagram.setText(it.data?.get("instagram").toString())
                binding.cbSelfProfileInstagram.isChecked = true
            }
        }

        binding.ibSelfProfileAvatar.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
        }

        binding.btnSelfProfileView.setOnClickListener {

            updating()

            FirebaseFirestore.getInstance().collection("detailedprofile").document(auth.uid).get().addOnSuccessListener {

                CommunityAdapter.transferprofileName = it.data?.get("name").toString()
                CommunityAdapter.transferprofileDesc = it.data?.get("desc").toString()
                CommunityAdapter.transferprofileAvatar = it.data?.get("photo").toString()
                CommunityAdapter.transferprofileFacebook = it.data?.get("facebook").toString()
                CommunityAdapter.transferprofileTwitter = it.data?.get("twitter").toString()
                CommunityAdapter.transferprofileInstagram = it.data?.get("instagram").toString()
                CommunityAdapter.transferprofileYoutube = it.data?.get("youtube").toString()
                CommunityAdapter.transferprofileUid = auth.uid

                (activity as MenuActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frMain, ProfilesFragment()).commit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            imageUri = data?.data
            binding.ibSelfProfileAvatar.load(imageUri)
        }
    }

    override fun onStop() {
        super.onStop()

        updating()
    }

    private fun updating() {

        val addingMap = hashMapOf<String, Any>()
        val easyMap = hashMapOf<String, Any>()

        addingMap["desc"] = binding.etSelfProfileExp.text.toString()

        if (!binding.etSelfProfileName.text.isNullOrEmpty()) {
            userProfileChangeRequest {
                displayName = binding.etSelfProfileName.text.toString()
            }

            addingMap["name"] = binding.etSelfProfileName.text.toString()
            easyMap["name"] = binding.etSelfProfileName.text.toString()

        }

        if (!binding.etSelfProfileInstagram.text.isNullOrEmpty() && binding.cbSelfProfileInstagram.isChecked) {
            addingMap["instagram"] = binding.etSelfProfileInstagram.text.toString()
        } else {
            addingMap["instagram"] = "null"
        }

        if (!binding.etSelfProfileFacebook.text.isNullOrEmpty() && binding.cbSelfProfileFacebook.isChecked) {
            addingMap["facebook"] = binding.etSelfProfileFacebook.text.toString()
        } else {
            addingMap["facebook"] = "null"
        }

        if (!binding.etSelfProfileTwitter.text.isNullOrEmpty() && binding.cbSelfProfileTwitter.isChecked) {
            addingMap["twitter"] = binding.etSelfProfileTwitter.text.toString()
        } else {
            addingMap["twitter"] = "null"
        }

        if (!binding.etSelfProfileYoutube.text.isNullOrEmpty() && binding.cbSelfProfileYoutube.isChecked) {
            addingMap["youtube"] = binding.etSelfProfileYoutube.text.toString()
        } else {
            addingMap["youtube"] = "null"
        }

        db.collection("detailedprofile").document(auth.uid).update(addingMap)
        db.collection("profile").document(auth.uid).update(easyMap)

        if (imageUri.toString() != "null") {
            val ref = Firebase.storage.reference.child("photos/${userUID}.jpg")

            ref.putFile(imageUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { Uring ->

                    val changing = userProfileChangeRequest {
                        photoUri = Uring
                    }

                    auth.updateProfile(changing)
                            .addOnSuccessListener {
                                easyMap.clear()
                                addingMap.clear()

                                easyMap["photo"] = Uring.toString()
                                addingMap["photo"] = Uring.toString()

                                db.collection("detailedprofile").document(auth.uid).update(addingMap)
                                db.collection("profile").document(auth.uid).update(easyMap)
                            }

                }
            }
        }

    }
}