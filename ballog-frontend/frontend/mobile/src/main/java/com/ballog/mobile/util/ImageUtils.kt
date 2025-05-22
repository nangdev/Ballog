package com.ballog.mobile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import android.graphics.Matrix
import android.media.ExifInterface

object ImageUtils {
    /**
     * Uri에서 File로 변환
     */
    fun uriToFile(context: Context, uri: Uri): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val extension = getFileExtension(context, uri)
            val file = File(context.cacheDir, "${UUID.randomUUID()}.$extension")
            
            // 파일 저장
            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 이미지 파일을 MultipartBody.Part로 변환
     */
    fun createMultipartFromFile(file: File, paramName: String): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, file.name, requestFile)
    }
    
    /**
     * 파일 확장자 추출
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri)
        return when {
            mime?.contains("jpeg") == true -> "jpg"
            mime?.contains("jpg") == true -> "jpg"
            mime?.contains("png") == true -> "png"
            else -> "jpg"  // 기본값
        }
    }
    
    /**
     * EXIF Orientation을 반영해 올바른 방향의 비트맵을 반환
     */
    fun fixImageOrientation(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * 이미지 리사이징 (EXIF Orientation 보정 포함)
     */
    fun resizeImage(file: File, maxWidth: Int = 1024, maxHeight: Int = 1024): File? {
        try {
            // EXIF Orientation 보정된 비트맵 생성
            val orientedBitmap = fixImageOrientation(file)

            // 축소 비율 계산
            var scale = 1
            while (orientedBitmap.width / scale > maxWidth || orientedBitmap.height / scale > maxHeight) {
                scale *= 2
            }

            // 리사이즈
            val resizedBitmap = Bitmap.createScaledBitmap(
                orientedBitmap,
                orientedBitmap.width / scale,
                orientedBitmap.height / scale,
                true
            )

            // 새 파일에 저장
            val resizedFile = File(file.parentFile, "resized_${file.name}")
            FileOutputStream(resizedFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            return resizedFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
} 
