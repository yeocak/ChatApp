package com.yeocak.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yeocak.chatapp.MessagingAdapter
import com.yeocak.chatapp.SingleMessage
import com.yeocak.chatapp.databinding.ActivityMessageBinding

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapting = MessagingAdapter(mutableListOf<SingleMessage>(
            SingleMessage(true,"Test1"),
            SingleMessage(true,"Hey bu bir deneme"),
            SingleMessage(false,"Deneme 2"),
            SingleMessage(true,"Daha geniş bir ifadeyle paragraf; bir duyguyu, bir düşünceyi bir isteği, bir durumu, bir öneriyi, olayın bir yönünü, yalnızca bir yönüyle anlatım tekniklerinden ve düşünceyi geliştirme yollarından yararlanarak anlatan yazı türüdür. Kelimeler cümleleri, cümleler paragrafları, paragraflar da yazıları oluşturur."),
            SingleMessage(false,"Daha geniş bir ifadeyle paragraf; bir duyguyu, bir düşünceyi bir isteği, bir durumu, bir öneriyi, olayın bir yönünü, yalnızca bir yönüyle anlatım tekniklerinden ve düşünceyi geliştirme yollarından yararlanarak anlatan yazı türüdür. Kelimeler cümleleri, cümleler paragrafları, paragraflar da yazıları oluşturur.")
            ))

        binding.rvMessaging.adapter = adapting
        binding.rvMessaging.layoutManager = LinearLayoutManager(this)

    }
}