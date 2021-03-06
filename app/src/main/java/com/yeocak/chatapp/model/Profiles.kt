package com.yeocak.chatapp.model

data class Profile(
        val uid: String,
        val name: String,
        val intro: String?,
        val facebook: String?,
        val youtube: String?,
        val twitter: String?,
        val instagram: String?,
        val version: String
)

data class Photo(
        val uid: String,
        val photo: String
)