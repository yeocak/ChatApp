package com.yeocak.chatapp.ui.menu.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import coil.load
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.utils.ImageConvert
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.ui.menu.MenuActivity
import com.yeocak.chatapp.ui.menu.MenuActivity.Companion.menuActivity
import com.yeocak.chatapp.databinding.FragmentSelfProfileBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.lang.Exception

class SelfProfileFragment : Fragment() {

    private lateinit var binding: FragmentSelfProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseUser

    private var imageUri: Uri? = null

    private lateinit var profileInfo : MutableMap<String, String>
    private var changeList = mutableMapOf<Int,Boolean>()

    private var version = 500

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelfProfileBinding.inflate(layoutInflater)

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth.currentUser!!

        takeVersion()

        profileInfo = DatabaseFun.takeSelfProfile()
        if(profileInfo["name"].isNullOrEmpty()){
            firstTimeDatabase()
        }

        updateProfileFromDatabase()
        updateProfileFromFirebase()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnSaveChanges.setOnClickListener {
            checkIsOkay()
        }

        binding.btnUndoChanges.setOnClickListener {
            refreshFragment()
        }

        binding.ibSelfProfileAvatar.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
        }


        binding.etSelfProfileName.doAfterTextChanged {
            if(it.toString() != profileInfo["name"]){
                changes(0,true)
            }
            else{
                changes(0,false)
            }
        }

        binding.etSelfProfileExp.doAfterTextChanged {
            if(it.toString() != profileInfo["intro"]){
                changes(1,true)
            }
            else{
                changes(1,false)
            }
        }

        if(binding.cbSelfProfileYoutube.isChecked){
            binding.etSelfProfileYoutube.doAfterTextChanged {
                if(it.toString() != profileInfo["youtube"]){
                    changes(2,true)
                }
                else{
                    changes(2, false)
                }
            }
        }

        if(binding.cbSelfProfileInstagram.isChecked){
            binding.etSelfProfileInstagram.doAfterTextChanged {
                if(it.toString() != profileInfo["instagram"]){
                    changes(3,true)
                }
                else{
                    changes(3, false)
                }
            }
        }

        if(binding.cbSelfProfileFacebook.isChecked){
            binding.etSelfProfileFacebook.doAfterTextChanged {
                if(it.toString() != profileInfo["facebook"]){
                    changes(4,true)
                }
                else{
                    changes(4, false)
                }
            }
        }

        if(binding.cbSelfProfileTwitter.isChecked){
            binding.etSelfProfileTwitter.doAfterTextChanged {
                if(it.toString() != profileInfo["twitter"]){
                    changes(5,true)
                }
                else{
                    changes(5, false)
                }
            }
        }

        binding.cbSelfProfileYoutube.setOnClickListener {
            if(binding.cbSelfProfileYoutube.isChecked == (profileInfo["youtube"].toString() == "null")){
                changes(6,true)
            }
            else{
                changes(6,false)
            }
        }

        binding.cbSelfProfileInstagram.setOnClickListener {
            if(binding.cbSelfProfileInstagram.isChecked == (profileInfo["instagram"].toString() == "null")){
                changes(7,true)
            }
            else{
                changes(7,false)
            }
        }

        binding.cbSelfProfileTwitter.setOnClickListener {
            if(binding.cbSelfProfileTwitter.isChecked == (profileInfo["twitter"].toString() == "null")){
                changes(8,true)
            }
            else{
                changes(8,false)
            }
        }

        binding.cbSelfProfileFacebook.setOnClickListener {
            if(binding.cbSelfProfileFacebook.isChecked == (profileInfo["facebook"].toString() == "null")){
                changes(9,true)
            }
            else{
                changes(9,false)
            }
        }


        binding.btnRemovePhoto.setOnClickListener {

            val alertBuilder = AlertDialog.Builder(activity as MenuActivity)
                .apply {
                    setTitle("Remove profile photo")
                    setMessage("Do you really want to remove your current profile photo?")
                    setPositiveButton("Remove"){ _, _ ->

                        val changing = userProfileChangeRequest {
                            photoUri = Uri.parse("null")
                        }
                        auth.updateProfile(changing)

                        db.collection("detailedprofile").document(userUID!!).update(hashMapOf<String, Any>("photo" to "null")).addOnSuccessListener {
                            db.collection("profile").document(userUID!!).update(hashMapOf<String, Any>("photo" to "null")).addOnSuccessListener {
                                binding.ibSelfProfileAvatar.load(R.drawable.ic_baseline_person_24)
                                binding.btnRemovePhoto.visibility = GONE
                            }
                        }


                    }
                    setNegativeButton("Cancel"){ _, _ -> }
                }

            alertBuilder.show()
        }

    }

    private fun refreshFragment(){
        val fm = fragmentManager!!.beginTransaction().replace(R.id.frMain, SelfProfileFragment())
        fm.commit()
    }

    private fun firstTimeDatabase(){
        auth.displayName?.let { DatabaseFun.changeSelfProfile(it) }
    }

    private fun updateProfileFromFirebase(){

        var intro: String?= null
        var instagram: String?= null
        var facebook: String?= null
        var youtube: String?= null
        var twitter: String?= null

        var photo: String? = null

        db.collection("detailedprofile").document(userUID!!).get().addOnSuccessListener {
            GlobalScope.launch {

                var name = auth.displayName!!

                val first = ImageConvert.downloadImageBitmap(auth.photoUrl.toString(), menuActivity!!)
                val imageString = ImageConvert.getImageString(first)

                photo = imageString

                if(auth.photoUrl.toString() != "null"){
                    withContext(Main){
                        binding.btnRemovePhoto.visibility = VISIBLE
                    }
                }
                else{
                    photo = "null"
                    binding.ibSelfProfileAvatar.load(R.drawable.ic_baseline_person_24)
                }

                intro = it.data?.get("desc").toString()

                facebook = it.data?.get("facebook").toString()
                youtube = it.data?.get("youtube").toString()
                twitter = it.data?.get("twitter").toString()
                instagram = it.data?.get("instagram").toString()

                DatabaseFun.changeSelfProfile(name, photo, intro, youtube, instagram, facebook, twitter)

                withContext(Main){
                    updateProfileFromDatabase()
                }
            }
        }
    }

    private fun updateProfileFromDatabase(){

        profileInfo = DatabaseFun.takeSelfProfile()

        binding.etSelfProfileName.setText(profileInfo["name"])

        if(profileInfo["photo"] != "null" || profileInfo["photo"].isNullOrEmpty()){
            GlobalScope.launch {
                try {
                    val image = ImageConvert.getBitmap(profileInfo["photo"])
                    binding.ibSelfProfileAvatar.load(image)
                }catch (e: Exception){
                    Log.d("Error", "Error In SelfProfileFragment")
                }
            }
        }
        else{
            binding.ibSelfProfileAvatar.load(R.drawable.ic_baseline_person_24)
        }

        if(profileInfo["intro"] != "null"){
            binding.etSelfProfileExp.setText(profileInfo["intro"])
        }

        if(profileInfo["youtube"] != "null"){
            binding.etSelfProfileYoutube.setText(profileInfo["youtube"])
            binding.cbSelfProfileYoutube.isChecked = true
        }

        if(profileInfo["facebook"] != "null"){
            binding.etSelfProfileFacebook.setText(profileInfo["facebook"])
            binding.cbSelfProfileFacebook.isChecked = true
        }

        if(profileInfo["twitter"] != "null"){
            binding.etSelfProfileTwitter.setText(profileInfo["twitter"])
            binding.cbSelfProfileTwitter.isChecked = true
        }

        if(profileInfo["instagram"] != "null"){
            binding.etSelfProfileInstagram.setText(profileInfo["instagram"])
            binding.cbSelfProfileInstagram.isChecked = true
        }
    }

    private fun updateProfile(name: String, intro: String?, youtube: String?, instagram: String?, facebook: String?, twitter: String?) {

        val addingMap = hashMapOf<String, Any>()
        val easyMap = hashMapOf<String, Any>()

        addingMap["version"] = version

        addingMap["name"] = name
        easyMap["name"] = name

        easyMap["photo"] = auth.photoUrl.toString()
        addingMap["photo"] = auth.photoUrl.toString()

        addingMap["desc"] = intro.toString()

        addingMap["youtube"] = youtube.toString()
        addingMap["instagram"] = instagram.toString()
        addingMap["facebook"] = facebook.toString()
        addingMap["twitter"] = twitter.toString()

        db.collection("detailedprofile").document(userUID!!).set(addingMap).addOnSuccessListener {
            db.collection("profile").document(userUID!!).set(easyMap).addOnSuccessListener {
                DatabaseFun.changeSelfProfile(name,profileInfo["photo"].toString() , intro, youtube, instagram, facebook, twitter)
                refreshFragment()
            }
        }

    }

    private fun changes(index: Int, value: Boolean){
        changeList[index] = value
        if(changeList.values.contains(true)){
            binding.layoutChanges.visibility = VISIBLE
        }
        else{
            binding.layoutChanges.visibility = GONE
        }
    }

    private fun checkIsOkay(){

        var name: String?= null
        var intro: String?= null
        var instagram: String?= null
        var facebook: String?= null
        var youtube: String?= null
        var twitter: String?= null

        var errorMessage: String? = null

        if(!binding.etSelfProfileName.text.isNullOrEmpty() && binding.etSelfProfileName.text.toString() != "null"){
            name = binding.etSelfProfileName.text.toString()
        }
        else{
            errorMessage = "You must enter a valid name!"
        }

        if(binding.etSelfProfileExp.text.toString() != "null"){
            intro = binding.etSelfProfileExp.text.toString()
        }
        else{
            errorMessage = "You can't write null to your introduction"
        }
        if(binding.cbSelfProfileFacebook.isChecked && !binding.etSelfProfileFacebook.text.isNullOrEmpty() && binding.etSelfProfileFacebook.text.toString() != "null"){
            facebook = binding.etSelfProfileFacebook.text.toString()
        }

        if(binding.cbSelfProfileTwitter.isChecked && !binding.etSelfProfileTwitter.text.isNullOrEmpty() && binding.etSelfProfileTwitter.text.toString() != "null"){
            twitter = binding.etSelfProfileTwitter.text.toString()
        }

        if(binding.cbSelfProfileYoutube.isChecked && !binding.etSelfProfileYoutube.text.isNullOrEmpty() && binding.etSelfProfileYoutube.text.toString() != "null"){
            youtube = binding.etSelfProfileYoutube.text.toString()
        }

        if(binding.cbSelfProfileInstagram.isChecked && !binding.etSelfProfileInstagram.text.isNullOrEmpty() && binding.etSelfProfileInstagram.text.toString() != "null"){
            instagram = binding.etSelfProfileInstagram.text.toString()
        }

        if (errorMessage == null){
            updateProfile(name!!,intro, youtube, instagram, facebook, twitter)
        }
        else{
            Toast.makeText((activity as MenuActivity), errorMessage, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            binding.pbAvatar.visibility = VISIBLE

            imageUri = data?.data

            if (imageUri.toString() != "null") {
                uploadPhoto()
            }
        }
    }

    private fun uploadPhoto() {
        val ref = Firebase.storage.reference.child("photos/${userUID}.jpg")

        ref.putFile(imageUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { Uring ->

                val changing = userProfileChangeRequest {
                    photoUri = Uring
                }

                auth.updateProfile(changing).addOnSuccessListener {

                    val addingMap = hashMapOf<String, Any>()
                    val easyMap = hashMapOf<String, Any>()

                    addingMap["photo"] = Uring.toString()
                    easyMap["photo"] = Uring.toString()

                    addingMap["version"] = version

                    db.collection("detailedprofile").document(userUID!!).update(addingMap).addOnSuccessListener {
                        db.collection("profile").document(userUID!!).update(easyMap).addOnSuccessListener {
                            takeVersion()
                        }
                    }
                }

            }
        }.addOnCompleteListener {
            binding.ibSelfProfileAvatar.load(imageUri)
            binding.pbAvatar.visibility = GONE
        }
    }

    private fun takeVersion(){
        db.collection("detailedprofile").document(userUID!!).get().addOnSuccessListener {detailed ->
                version = detailed.data?.get("version")!!.toString().toInt()+1
        }
    }

}