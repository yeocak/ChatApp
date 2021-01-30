package com.yeocak.chatapp

import android.graphics.Bitmap

data class SingleMessages(
        var lastMessage: String,
        var uid : String,
        var name: String,
        var photo: String,
        var date: String
)