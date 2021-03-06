package com.yeocak.chatapp.ui.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.utils.LoginData
import com.yeocak.chatapp.ui.menu.MenuActivity
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