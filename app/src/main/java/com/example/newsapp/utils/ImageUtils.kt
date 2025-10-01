package com.example.newsapp.utils

import android.content.Context
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream


object ImageUtils {

//     Downloads an image (via Glide) and saves it to internal files dir.
    fun saveImageLocally(context: Context, imageUrl: String, fileNamePrefix: String): String? {
        return try {
            val future = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit()

            val bitmap = future.get()
            Glide.with(context).clear(future)

            val file = File(context.filesDir, "$fileNamePrefix.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
