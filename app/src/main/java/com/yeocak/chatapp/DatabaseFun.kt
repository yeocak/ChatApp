package com.yeocak.chatapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getBlobOrNull

object DatabaseFun {

        private lateinit var databasing : SQLiteDatabase

        fun creating(context: Context, userID : String) {
            databasing = context.openOrCreateDatabase(userID, Context.MODE_PRIVATE,null)
        }

        fun setup(tableName : String){
            databasing.execSQL("CREATE TABLE IF NOT EXISTS $tableName (fromid TEXT PRIMARY KEY, message TEXT, name TEXT, photo TEXT, date TEXT)")
        }

        fun add(tableName: String, fromID: String, message: String, name: String, photo: String? , date: String){
            databasing.execSQL("INSERT OR REPLACE INTO $tableName (fromid ,message, name, photo, date) VALUES ('$fromID' , '$message' , '$name' , '$photo' , '$date')")
        }

        fun take(tableName: String): MutableList<SingleMessages>{
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

}