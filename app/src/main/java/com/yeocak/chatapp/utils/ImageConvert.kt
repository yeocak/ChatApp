package com.yeocak.chatapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.ByteArrayOutputStream
import java.lang.Exception



object ImageConvert {

    fun getImageString(image: Bitmap?) : String?{
        val baos = ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.PNG, 0, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun getBitmap(image: String?): Bitmap? {
        val decodedByteArray: ByteArray = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
    }

    suspend fun downloadImageBitmap(imageURL: String, context: Context, width: Int = 100, heigth: Int = 100): Bitmap? {

        var bitmapNow: Bitmap? = null

        try {
            val request = ImageRequest.Builder(context)
                .data(imageURL)
                .target{
                    bitmapNow = (it as BitmapDrawable).bitmap
                }
                .build()


            context.imageLoader.execute(request)

            if(bitmapNow != null){
                return Bitmap.createScaledBitmap(
                        bitmapNow!!,
                        width,
                        heigth,
                        false
                )
            }

        }catch (e: Exception){
            FirebaseCrashlytics.getInstance().setCustomKey("imageDownload", "Error Image Converting : ${e.localizedMessage}")

        }

        return bitmapNow
    }

}