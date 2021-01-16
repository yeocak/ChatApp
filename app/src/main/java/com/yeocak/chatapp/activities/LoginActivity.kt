package com.yeocak.chatapp.activities

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bolts.Task
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityLoginBinding

    private lateinit var googleClient: GoogleSignInClient
    private lateinit var googleOptions: GoogleSignInOptions
    private var RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if(auth.currentUser != null){
            updateActivity()
        }

        callbackManager = CallbackManager.Factory.create()

        binding.btnFacebook.setOnClickListener{
            binding.pbLogin.visibility = VISIBLE
            signFacebook()
        }

        googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleClient = GoogleSignIn.getClient(this,googleOptions)
        binding.btnGoogle.setOnClickListener {
            binding.pbLogin.visibility = VISIBLE
            signGoogle()
        }

    }

    private fun signGoogle(){
        val signIntent = googleClient.signInIntent
        startActivityForResult(signIntent,RC_SIGN_IN)
    }

    private fun signFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))

        LoginManager.getInstance().registerCallback(this.callbackManager, object :
                FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("Facebook", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("Facebook", "facebook:onCancel")
                binding.pbLogin.visibility = INVISIBLE
            }

            override fun onError(error: FacebookException) {
                Log.d("Facebook", "facebook:onError", error)
                binding.pbLogin.visibility = INVISIBLE
            }

        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("Facebook", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateActivity()
                } else {
                    Toast.makeText(this,"Authentication failed!",Toast.LENGTH_SHORT).show()
                    binding.pbLogin.visibility = INVISIBLE
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val acc = task.getResult(ApiException::class.java)!!

                val credential = GoogleAuthProvider.getCredential(acc.idToken, null)
                auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                updateActivity()
                            } else {
                                Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
            }catch(e: ApiException){
                Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show()
            }
            binding.pbLogin.visibility = INVISIBLE
        }

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun goSignUp(view: View){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goSignIn(view: View){
        binding.pbLogin.visibility = VISIBLE
        auth.signInWithEmailAndPassword(binding.etLoginEmail.text.toString(),binding.etLoginPassword.text.toString())
            .addOnCompleteListener(this) {
                if(it.isSuccessful){
                    updateActivity()
                }
                else{
                    Toast.makeText(this,"Authentication failed!",Toast.LENGTH_SHORT).show()
                    binding.pbLogin.visibility = INVISIBLE
                }
            }
    }

    private fun updateActivity(){
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        binding.pbLogin.visibility = INVISIBLE
        finish()
    }
}