package com.example.group_g_mobile.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class InternalFileManager(private val context: Context) {

    companion object {
        private const val AVATAR_FILENAME = "avatar.jpg"
    }


    fun saveAvatar(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, AVATAR_FILENAME)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getAvatarFile(): File {
        return File(context.filesDir, AVATAR_FILENAME)
    }

    fun getAvatarPath(): String? {
        val file = getAvatarFile()
        return if (file.exists()) file.absolutePath else null
    }

    fun deleteAvatar(): Boolean {
        val file = getAvatarFile()
        return if (file.exists()) file.delete() else false
    }
}
