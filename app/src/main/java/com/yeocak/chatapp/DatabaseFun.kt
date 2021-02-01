package com.yeocak.chatapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getBlobOrNull

object DatabaseFun {

    private lateinit var databasing : SQLiteDatabase

    fun creating(context: Context, userID : String) {
        databasing = context.openOrCreateDatabase(userID, Context.MODE_PRIVATE,null)
    }

    fun setupLast(tableName : String){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS $tableName (fromid TEXT PRIMARY KEY, message TEXT, name TEXT, photo TEXT, date TEXT)")
    }

    fun addLastMessage(tableName: String, fromID: String, message: String, name: String, photo: String?, date: String){
        databasing.execSQL("INSERT OR REPLACE INTO $tableName (fromid ,message, name, photo, date) VALUES ('$fromID' , '$message' , '$name' , '$photo' , '$date')")
    }

    fun takeLastMessages(tableName: String): MutableList<SingleMessages>{
        val cursor = databasing.rawQuery("SELECT * FROM $tableName", null)

        val returnList = mutableListOf<SingleMessages>()

        while(cursor.moveToNext()){
            returnList.add(
                SingleMessages(
                    cursor.getString(1),
                    cursor.getString(0),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                )
            )
        }
        cursor.close()

        return returnList
    }

    fun setupSelfProfile(){
        databasing.execSQL("CREATE TABLE IF NOT EXISTS profile (system TEXT PRIMARY KEY, name TEXT, photo TEXT, intro TEXT, youtube TEXT, instagram TEXT, facebook TEXT, twitter TEXT)")
    }

    fun changeSelfProfile(name: String, photo: String? = "null", intro: String? = "null", youtube: String? = "null", instagram: String? = "null", facebook: String? = "null", twitter: String? = "null", system: String? = "1"){
        databasing.execSQL("INSERT OR REPLACE INTO profile (system, name, photo, intro, youtube, instagram, facebook, twitter) VALUES ('$system' , '$name' , '$photo' , '$intro' , '$youtube' , " +
                "'$instagram' , '$facebook' , '$twitter')")
    }

    fun takeSelfProfile(): MutableMap<String, String>{
        val cursor = databasing.rawQuery("SELECT * FROM profile", null)

        val returnList = mutableMapOf<String, String>()

        while(cursor.moveToNext()){
            returnList["name"] = cursor.getString(1)
            returnList["photo"] = cursor.getString(2)
            returnList["intro"] = cursor.getString(3)
            returnList["youtube"] = cursor.getString(4)
            returnList["instagram"] = cursor.getString(5)
            returnList["facebook"] = cursor.getString(6)
            returnList["twitter"] = cursor.getString(7)
        }
        cursor.close()

        return returnList
    }

}