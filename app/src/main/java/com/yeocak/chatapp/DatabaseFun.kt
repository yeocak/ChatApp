package com.yeocak.chatapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.yeocak.chatapp.activities.MessageActivity

object DatabaseFun {

        private lateinit var databasing : SQLiteDatabase

        fun creating(context: Context, userID : String) {
            databasing = context.openOrCreateDatabase(userID, Context.MODE_PRIVATE,null)
        }

        fun setup(tableName : String){
            databasing.execSQL("CREATE TABLE IF NOT EXISTS $tableName (fromid TEXT PRIMARY KEY, message TEXT, date TEXT)")
        }

        fun add(tableName: String, fromID: String, message: String, date: String){
            databasing.execSQL("INSERT OR REPLACE INTO $tableName (fromid ,message, date) VALUES ('$fromID' , '$message' , '$date')")
        }

        fun take(tableName: String): MutableList<SingleMessages>{
            val cursor = databasing.rawQuery("SELECT * FROM $tableName", null)

            val returnList = mutableListOf<SingleMessages>()

            while(cursor.moveToNext()){
                returnList.add(SingleMessages(
                        cursor.getString(1),
                        cursor.getString(0),
                        cursor.getString(2)
                ))
            }

            cursor.close()

            return returnList
        }

}