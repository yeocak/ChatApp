package com.yeocak.chatapp.ui.menu.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.utils.ImageConvert
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.ui.menu.MenuActivity
import com.yeocak.chatapp.ui.message.MessageActivity
import com.yeocak.chatapp.database.DatabaseFun
import com.yeocak.chatapp.model.Photo
import com.yeocak.chatapp.model.Profile
import com.yeocak.chatapp.databinding.FragmentProfilesBinding
import com.yeocak.chatapp.ui.menu.fragments.CommunityAdapter.Companion.transferprofileUid
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ProfilesFragment : Fragment() {

    private lateinit var binding : FragmentProfilesBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fsdb = FirebaseFirestore.getInstance().collection("detailedprofile").document(transferprofileUid!!)

        GlobalScope.launch {
            fsdb.get().addOnSuccessListener { data ->
                val newProf = Profile(
                        transferprofileUid!!,
                        data["name"].toString(),
                        data["desc"].toString(),
                        data["facebook"].toString(),
                        data["youtube"].toString(),
                        data["twitter"].toString(),
                        data["instagram"].toString(),
                        data["version"].toString()
                )

                DatabaseFun.addProfile(newProf)

                MainScope().launch {
                    val bitmap = ImageConvert.downloadImageBitmap(data["photo"].toString(),(activity as MenuActivity))
                    val string = ImageConvert.getImageString(bitmap)

                    if(string != null){
                        DatabaseFun.addPhoto(Photo(
                                transferprofileUid!!,
                                string
                        ))
                    }
                    setProfile(data["photo"].toString())
                }
            }
        }

        binding.btnBlockUser.isVisible = (userUID != transferprofileUid)

        binding.btnProfileSendMessage.setOnClickListener {

            val intent = Intent((activity as MenuActivity), MessageActivity::class.java)
            intent.putExtra("uid", transferprofileUid)
            startActivity(intent)

        }

        binding.btnBlockUser.setOnClickListener {

            val alertBuilder = AlertDialog.Builder(activity as MenuActivity)
                .apply {
                    setTitle("Block user")
                    setMessage("Do you really want to block this person from sending you messages and see your profile?")
                    setPositiveButton("Block"){ _, _ ->

                        FirebaseFirestore.getInstance().collection("block").document(userUID!!).collection("to").document(
                            transferprofileUid!!).set(hashMapOf<String,Any>("is" to true))
                            .continueWith {
                                FirebaseFirestore.getInstance().collection("block").document(transferprofileUid!!).collection("from").document(
                                    userUID!!).set(hashMapOf<String,Any>("is" to true))
                            }.addOnCompleteListener {

                                if(it.isSuccessful){
                                    Toast.makeText((activity as MenuActivity), "User blocked", Toast.LENGTH_SHORT).show()

                                    (activity as MenuActivity).supportFragmentManager.beginTransaction()
                                        .replace(R.id.frMain, CommunityFragment()).commit()
                                }
                                else{
                                    Toast.makeText((activity as MenuActivity), "Something went wrong", Toast.LENGTH_SHORT).show()
                                }
                            }

                    }
                    setNegativeButton("Cancel"){ _, _ -> }
                }

            alertBuilder.show()

        }

    }

    private fun setProfile(imageUrl: String){
        val prof = DatabaseFun.takeProfile(transferprofileUid!!)!!
        binding.tvProfileName.text = prof.name
        if(prof.intro != "null"){
            binding.tvProfileExp.text = prof.intro
        }
        if(prof.instagram != "null"){
            binding.tvProfileInstagram.text = prof.instagram
            binding.layoutProfileInstagram.visibility = VISIBLE
        }
        if(prof.youtube != "null"){
            binding.tvProfileYoutube.text = prof.youtube
            binding.layoutProfileYoutube.visibility = VISIBLE
        }
        if(prof.facebook != "null"){
            binding.tvProfileFacebook.text = prof.facebook
            binding.layoutProfileFacebook.visibility = VISIBLE
        }
        if(prof.twitter != "null"){
            binding.tvProfileTwitter.text = prof.twitter
            binding.layoutProfileTwitter.visibility = VISIBLE
        }

        if(imageUrl != "null"){
            binding.ivProfileAvatar.load(imageUrl)
        }
    }
}