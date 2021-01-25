package com.yeocak.chatapp

data class SingleMessage(
    var isOwner : Boolean,
    var message : String
)

data class RealtimeMessage(
    var title: String, // this is sender name
    var message: String,
    var fromUID : String
)