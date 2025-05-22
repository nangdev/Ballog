package com.ballog.mobile.util

import android.content.Context
import java.io.File

object FileUtils {
    fun uriToFile(context: Context, uri: android.net.Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val file = File.createTempFile("upload_", ".mp4", context.cacheDir)
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }
}
