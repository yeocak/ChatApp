package com.yeocak.chatapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.yeocak.chatapp.activities.MessageActivity

object DatabaseFun {

        private lateinit var databasing : SQLiteDatabase

        fun creating(context: Context, userID : String) {
            databasing = context.openOrCreateDatabase(userID, Context.MODE_PRIVATE,null)
        }

        fun setup(fromID: String){
            databasing.execSQL("CREATE TABLE IF NOT EXISTS $fromID (id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT, isown TEXT)")
        }

        fun add(fromID: String, message: String, isOwn : Boolean){
            databasing.execSQL("INSERT INTO $fromID (message, isown) VALUES ('$message' , '$isOwn')")
        }

        fun take(fromID: String, startPoint: Int = 0): MutableList<SingleMessage>{
            val cursor = databasing.rawQuery("SELECT * FROM $fromID WHERE (id >= $startPoint AND id <= ${startPoint+40})", null)

            val returnList = mutableListOf<SingleMessage>()

            while(cursor.moveToNext()){
                returnList.add(SingleMessage(
                        cursor.getString(2).toBoolean() , cursor.getString(1)
                ))
            }
            cursor.close()

            return returnList
        }

}