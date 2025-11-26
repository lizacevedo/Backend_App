package com.example.monitoreo_happypet.remote.roboflow



import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object MultipartUtil {

    fun crearMultipartDesdeUri(context: Context, uri: Uri): MultipartBody.Part {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            "file",          // ➜ debe llamarse EXACTAMENTE igual que en el backend
            "imagen.jpg",
            requestFile
        )
    }
}
