package com.yeocak.chatapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.yeocak.chatapp.ImageConvert
import com.yeocak.chatapp.LoginData.userUID
import kotlinx.coroutines.*

object DatabaseFun {

    private lateinit var databasing : SQLiteDatabase

    fun creating(context: Context, userID : String) {
        databasing = context.openOrCreateDatabase(userID, Context.MODE_PRIVATE,null)
    }

    fun setupMessage(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS messages (uniq TEXT PRIMARY KEY, fromid TEXT, message TEXT, photo TEXT, date TEXT, isowner BOOL)")
    }

    fun addMessage(message: Message){
        databasing.execSQL("INSERT OR REPLACE INTO messages (uniq, fromid, message, photo, date, isowner) VALUES ('${message.uniq}','${message.fromId}'," +
                "'${message.message}','${message.photo}','${message.date}','${message.isOwner}')")
    }

    fun takeMessage(fromID: String): MutableList<Message>{
        val cursor = databasing.rawQuery("SELECT * FROM messages WHERE fromid == '$fromID' ORDER BY date DESC LIMIT 20", null)

        val returnList = mutableListOf<Message>()

        while (cursor.moveToNext()){
            returnList.add(
                Message(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5).toBoolean()
                )
            )
        }
        cursor.close()

        return returnList.reversed().toMutableList()
    }

    fun setupProfile(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS profiles (uid TEXT PRIMARY KEY, name TEXT, intro TEXT, facebook TEXT, youtube TEXT, twitter TEXT, instagram TEXT, version TEXT)")
    }

    fun addProfile(profile: Profile){
        databasing.execSQL("INSERT OR REPLACE INTO profiles (uid, name, intro, facebook, youtube, twitter, instagram, version) VALUES ('${profile.uid}','${profile.name}'," +
                "'${profile.intro}','${profile.facebook}','${profile.youtube}','${profile.twitter}','${profile.instagram}','${profile.version}')")
    }

    fun takeProfile(uid: String): Profile?{
        val cursor = databasing.rawQuery("SELECT * FROM profiles WHERE uid == '$uid'", null)

        var returnProf: Profile? = null

        while (cursor.moveToNext()){
                returnProf = Profile(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                )
        }
        cursor.close()

        return returnProf
    }

    fun setupLast(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS lasts (uid TEXT PRIMARY KEY, message TEXT, date TEXT)")
    }

    fun addLast(last: LastMessage){
        databasing.execSQL("INSERT OR REPLACE INTO lasts (uid, message, date) VALUES ('${last.uid}','${last.message}','${last.date}')")
    }

    fun takeLasts(): MutableList<LastMessage>{
        val cursor = databasing.rawQuery("SELECT * FROM lasts ORDER BY date", null)

        val returnLast = mutableListOf<LastMessage>()

        while (cursor.moveToNext()){
            returnLast.add(
                    LastMessage(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2)
                    )
            )
        }
        cursor.close()

        return returnLast
    }

    fun setupPhoto(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS photo (uid TEXT PRIMARY KEY, photo TEXT)")
    }

    fun addPhoto(data: Photo){
        databasing.execSQL("INSERT OR REPLACE INTO photo (uid, photo) VALUES ('${data.uid}', '${data.photo}')")
    }

    fun takePhoto(uid: String): Photo?{
        val cursor = databasing.rawQuery("SELECT * FROM photo WHERE uid == '$uid'", null)

        var returning: Photo? = null

        while (cursor.moveToNext()){
            returning = Photo(
                    uid,
                    cursor.getString(1)
            )
        }
        cursor.close()

        return returning
    }

    fun setupSelfProfile(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS selfprofile (uid TEXT PRIMARY KEY, name TEXT, photo TEXT, intro TEXT, youtube TEXT," +
                "facebook TEXT, twitter TEXT, instagram TEXT)")
    }

    fun changeSelfProfile(name: String, photo: String? = null, intro: String? = null, youtube: String? = null, instagram: String? = null, facebook: String? = null, twitter: String? = null){
        databasing.execSQL("INSERT OR REPLACE INTO selfprofile (uid, name, photo, intro, youtube," +
                "facebook, twitter, instagram) VALUES ('$userUID','$name','$photo','$intro','$youtube','$facebook','$twitter','$instagram')")
    }

    fun takeSelfProfile(): MutableMap<String, String>{
        val cursor = databasing.rawQuery("SELECT * FROM selfprofile", null)

        var returning = mutableMapOf<String, String>()

        while (cursor.moveToNext()){
            returning = mutableMapOf(
                    "name" to cursor.getString(1),
                    "photo" to cursor.getString(2),
                    "intro" to cursor.getString(3),
                    "youtube" to cursor.getString(4),
                    "facebook" to cursor.getString(5),
                    "twitter" to cursor.getString(6),
                    "instagram" to cursor.getString(7)
            )
        }
        cursor.close()

        return returning
    }

    suspend fun String.getProfile(context: Context): String{
        var result = "nothing"
        val fsdb = FirebaseFirestore.getInstance().collection("detailedprofile").document(this)

        var profiling: Profile? = null
        profiling = takeProfile(this)

        if(profiling != null){
            fsdb.get().addOnSuccessListener {
                Log.d("Imaging", it["version"].toString() + " | " + profiling.version)
                if(it["version"].toString() != profiling.version){
                    val newProf = Profile(
                            this@getProfile,
                            it["name"].toString(),
                            it["intro"].toString(),
                            it["facebook"].toString(),
                            it["youtube"].toString(),
                            it["twitter"].toString(),
                            it["instagram"].toString(),
                            it["version"].toString()
                    )

                    addProfile(newProf)
                    MainScope().launch {
                        val bitmap = ImageConvert.downloadImageBitmap(it["photo"].toString(),context)
                        val string = ImageConvert.getImageString(bitmap)

                        if(string != null){
                            addPhoto(Photo(
                                    this@getProfile,
                                    string
                            ))
                        }
                    }
                }

            }
        }
        else{
            result = "download"
        }

        Log.d("Imaging",result)
        return result
    }
}