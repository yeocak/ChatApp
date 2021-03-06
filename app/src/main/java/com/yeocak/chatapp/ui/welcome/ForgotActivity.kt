package com.yeocak.chatapp.ui.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.databinding.ActivityForgotBinding

class ForgotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        binding.btnSendEmail.setOnClickListener {
            Firebase.auth.sendPasswordResetEmail(binding.etForgotEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"Email sent!",Toast.LENGTH_SHORT).show()
                        binding.etForgotEmail.setText("")
                    }
                    else{
                        Toast.makeText(this,"Email couldn't be sent",Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}