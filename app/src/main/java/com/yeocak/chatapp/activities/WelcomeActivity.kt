package com.yeocak.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.ktx.auth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.DatabaseFun
import com.yeocak.chatapp.LoginData
import com.yeocak.chatapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseInstanceId.getInstance().token?.let {
            LoginData.phoneToken = it
        }

        if(Firebase.auth.currentUser != null){

            LoginData.userUID = Firebase.auth.currentUser!!.uid
            DatabaseFun.creating(this,Firebase.auth.currentUser!!.uid)

            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finishAffinity()

        }
        else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }
}