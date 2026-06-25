package com.example.proyect_final.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtils {
    /**
     * Saves a bitmap to the internal storage and returns the absolute path.
     */
    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        val directory = File(context.filesDir, "captured_garments")
        if (!directory.exists()) directory.mkdirs()
        
        val fileName = "garment_${UUID.randomUUID()}.jpg"
        val file = File(directory, fileName)
        
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Scales down a bitmap if it exceeds the maximum dimension, preserving aspect ratio.
     */
    fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (ratio > 1) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Reads a URI as a bitmap, compresses/scales it, and saves it to internal storage.
     * Returns the absolute path of the saved file.
     */
    fun saveAvatarFromUri(context: Context, uri: android.net.Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val scaled = scaleBitmap(bitmap, 256)
                    val directory = File(context.filesDir, "avatars")
                    if (!directory.exists()) directory.mkdirs()
                    val file = File(directory, "avatar_current.jpg")
                    FileOutputStream(file).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    file.absolutePath
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
