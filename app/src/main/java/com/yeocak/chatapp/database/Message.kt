package com.yeocak.chatapp.database

data class Message(
        val uniq: String,
        val fromId: String,
        val message: String,
        val photo: String?,
        val date: String,
        val isOwner: Boolean
)