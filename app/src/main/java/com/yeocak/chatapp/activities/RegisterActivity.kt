package com.yeocak.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGoBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnRegister.setOnClickListener {
            binding.pbRegister.visibility = VISIBLE
            registerCheck()
        }
    }

    private fun registerCheck(){

        var isOkay = true
        var toastMessage = "Failed"

        if(binding.etRegisterConfirm.text.toString() == binding.etRegisterPassword.text.toString() && binding.etRegisterConfirm.text.isNotEmpty()){
            binding.etRegisterConfirm.setBackgroundResource(R.drawable.custom_input_true)
        }
        else{
            binding.etRegisterConfirm.setBackgroundResource(R.drawable.custom_input_false)
            isOkay = false
            toastMessage = "Passwords doesn't match!"
        }

        if(binding.etRegisterPassword.text.isNotEmpty() && binding.etRegisterPassword.text.length >= 8 && binding.etRegisterPassword.text.length <= 16){
            binding.etRegisterPassword.setBackgroundResource(R.drawable.custom_input_true)
        }
        else{
            binding.etRegisterPassword.setBackgroundResource(R.drawable.custom_input_false)
            isOkay = false
            toastMessage = "Password length should be between 8 and 16."
        }

        if(binding.etRegisterEmail.text.isNotEmpty() && binding.etRegisterEmail.text.contains('@') && binding.etRegisterEmail.text.contains(".com")){
            binding.etRegisterEmail.setBackgroundResource(R.drawable.custom_input_true)
        }
        else{
            binding.etRegisterEmail.setBackgroundResource(R.drawable.custom_input_false)
            isOkay = false
            toastMessage = "Please, write a real email."
        }

        if(binding.etRegisterUsername.text.isNotEmpty()){
            binding.etRegisterUsername.setBackgroundResource(R.drawable.custom_input_true)
        }
        else{
            binding.etRegisterUsername.setBackgroundResource(R.drawable.custom_input_false)
            isOkay = false
            toastMessage = "Please fill the name field."
        }

        if(isOkay){
            registerDone()
        }
        else{
            binding.pbRegister.visibility = INVISIBLE
            Toast.makeText(this,toastMessage,Toast.LENGTH_SHORT).show()
        }

    }

    private fun registerDone(){
        auth.createUserWithEmailAndPassword(binding.etRegisterEmail.text.toString(),binding.etRegisterPassword.text.toString())
            .addOnCompleteListener(this){
                if(it.isSuccessful){

                    val user = Firebase.auth.currentUser

                    val profileUpdates = userProfileChangeRequest {
                        displayName = binding.etRegisterUsername.text.toString()
                    }

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, MenuActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else{
                                Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                else{
                    Toast.makeText(this,it.exception.toString(),Toast.LENGTH_SHORT).show()
                }
                binding.pbRegister.visibility = INVISIBLE
            }
    }
}