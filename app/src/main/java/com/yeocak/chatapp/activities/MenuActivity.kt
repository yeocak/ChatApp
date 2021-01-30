package com.yeocak.chatapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.DisplayMetrics
import android.util.Log
import com.yeocak.chatapp.DatabaseFun
import com.yeocak.chatapp.ImageConvert
import com.yeocak.chatapp.R
import com.yeocak.chatapp.databinding.ActivityMenuBinding
import com.yeocak.chatapp.fragments.CommunityFragment
import com.yeocak.chatapp.fragments.CommunityFragment.Companion.transferinWidth
import com.yeocak.chatapp.fragments.MessagesFragment
import com.yeocak.chatapp.fragments.SelfProfileFragment
import com.yeocak.chatapp.fragments.SettingsFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {

    lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager?.defaultDisplay!!.getRealMetrics(displayMetrics)
        transferinWidth = displayMetrics.xdpi

        supportFragmentManager.beginTransaction().replace(R.id.frMain, MessagesFragment()).commit()

        binding.bottomNavigation.setOnNavigationItemSelectedListener {

            val transaction = supportFragmentManager.beginTransaction()

            when (it.itemId) {
                R.id.ic_messages -> {
                    transaction.replace(R.id.frMain, MessagesFragment()).commit()
                    true
                }
                R.id.ic_community -> {
                    transaction.replace(R.id.frMain, CommunityFragment()).commit()
                    true
                }
                R.id.ic_profile -> {
                    transaction.replace(R.id.frMain, SelfProfileFragment()).commit()
                    true
                }
                R.id.ic_settings -> {
                    transaction.replace(R.id.frMain, SettingsFragment()).commit()
                    true
                }
                else -> false
            }
        }

    }

}