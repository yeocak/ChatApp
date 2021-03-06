package com.yeocak.chatapp.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.yeocak.chatapp.utils.LoginData.phoneToken
import com.yeocak.chatapp.utils.LoginData.userUID
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityLoginBinding

    private lateinit var googleClient: GoogleSignInClient
    private lateinit var googleOptions: GoogleSignInOptions

    private lateinit var githubProvider : OAuthProvider.Builder

    private val RC_SIGN_IN = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if(auth.currentUser != null){
            updateActivity()
        }

        binding.btnGithub.setOnClickListener {
            githubProvider = OAuthProvider.newBuilder("github.com")
            binding.pbLogin.visibility = VISIBLE

            signGithub()
        }


        binding.btnGoogle!!.setOnClickListener {
            binding.pbLogin.visibility = VISIBLE
            signGoogle()
        }

        binding.tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }
    }

    private fun signGithub(){
        val pendingResultTask = Firebase.auth.pendingAuthResult

        if (pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener {
                        updateActivity()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Somethings went wrong! Code: 1", Toast.LENGTH_SHORT).show()
                        binding.pbLogin.visibility = GONE
                    }
        } else {
            Firebase.auth
                    .startActivityForSignInWithProvider(this, githubProvider.build())
                    .addOnSuccessListener(
                            OnSuccessListener<AuthResult?> {
                                updateActivity()
                            })
                    .addOnFailureListener(
                            OnFailureListener {
                                Toast.makeText(this, "Somethings went wrong! Code: 2", Toast.LENGTH_SHORT).show()
                                binding.pbLogin.visibility = GONE
                            })
        }
    }

    private fun signGoogle(){
        googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleClient = GoogleSignIn.getClient(this, googleOptions)

        val signIntent = googleClient.signInIntent
        startActivityForResult(signIntent, RC_SIGN_IN)
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
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
            }catch (e: ApiException){
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                FirebaseCrashlytics.getInstance().setCustomKey("sign", "Error Google Sign In : ${e.localizedMessage} | ${e.toString()}")
                Log.d("GoogleError", e.toString())
            }
            binding.pbLogin.visibility = GONE
        }
    }

    fun goSignUp(view: View){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goSignIn(view: View){

        if(binding.etLoginEmail.text.isNotEmpty() && binding.etLoginPassword.text.isNotEmpty()){
            binding.pbLogin.visibility = VISIBLE

            auth.signInWithEmailAndPassword(binding.etLoginEmail.text.toString(), binding.etLoginPassword.text.toString())
                    .addOnCompleteListener(this) {
                        if(it.isSuccessful){
                            updateActivity()
                        }
                        else{
                            Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                            binding.pbLogin.visibility = GONE
                        }
                    }
        }

    }

    private fun updateActivity(){

        FirebaseInstanceId.getInstance().token?.let {
            phoneToken = it
        }

        userUID = auth.currentUser?.uid

        val db = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()

        db.firestoreSettings = settings


        db.collection("detailedprofile").document(auth.currentUser!!.uid).get().addOnSuccessListener {

            if(it["name"].toString() == "null"){
                db.collection("block").document(auth.currentUser!!.uid).collection("from").document("system").set(hashMapOf<String, Any>())
                db.collection("block").document(auth.currentUser!!.uid).collection("to").document("system").set(hashMapOf<String, Any>())

                val userName = auth.currentUser?.displayName
                val inserting = HashMap<String, String>()
                inserting["currentPhone"] = phoneToken!!
                inserting["name"] = userName!!
                if(auth.currentUser?.photoUrl != null){
                    inserting["photo"] = auth.currentUser?.photoUrl.toString()
                }

                db.collection("profile").document(userUID!!).set(inserting).addOnSuccessListener {

                    inserting.clear()
                    inserting["name"] = auth.currentUser?.displayName.toString()
                    inserting["version"] = "0"
                    if(auth.currentUser?.photoUrl != null){
                        inserting["photo"] = auth.currentUser?.photoUrl.toString()
                    }

                    db.collection("detailedprofile").document(userUID!!).set(inserting).addOnSuccessListener {

                        val intent = Intent(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        binding.pbLogin.visibility = GONE

                    }

                }
            }
            else{
                db.collection("profile").document(userUID!!).update(mapOf("currentPhone" to phoneToken!!))

                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                binding.pbLogin.visibility = GONE
            }

        }.addOnFailureListener {
            if(auth.currentUser!=null){
                auth.signOut()
                Toast.makeText(this,"Something went wrong!",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                binding.pbLogin.visibility = GONE
            }
        }
    }
}