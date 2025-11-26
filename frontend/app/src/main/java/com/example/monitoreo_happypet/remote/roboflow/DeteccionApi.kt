package com.example.monitoreo_happypet.remote.roboflow



import com.example.monitoreo_happypet.model.roboflow.DeteccionRespuesta
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DeteccionApi {

    @Multipart
    @POST("api/detecciones/imagen")
    suspend fun detectarEnImagen(
        @Part file: MultipartBody.Part
    ): Response<DeteccionRespuesta>
}
