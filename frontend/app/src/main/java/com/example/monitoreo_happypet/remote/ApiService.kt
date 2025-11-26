package com.example.monitoreo_happypet.remote

import com.example.monitoreo_happypet.model.LoginRequest
import com.example.monitoreo_happypet.model.AuthResponse
import com.example.monitoreo_happypet.model.GoogleLoginRequest
import com.example.monitoreo_happypet.model.RegisterRequest
import com.example.monitoreo_happypet.model.ResetPasswordRequest
import com.example.monitoreo_happypet.model.mascotas.Mascota
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/usuarios/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/usuarios/registrar")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/usuarios/login-google")
    suspend fun loginGoogle(@Body body: GoogleLoginRequest): AuthResponse

    @POST("api/usuarios/recuperar")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Map<String, Any>

    // NUEVO: Endpoint para actualizar contraseña directamente
    @POST("api/usuarios/actualizar-contraseña")
    suspend fun actualizarContraseña(@Body body: Map<String, String>): Map<String, Any>

    @GET("api/usuarios/me")
    suspend fun me(): Map<String, Any>

    @GET("api/mascotas/usuario/{usuarioId}")
    suspend fun obtenerMascotas(@Path("usuarioId") usuarioId: Long): List<Mascota>

    @POST("api/mascotas/{usuarioId}")
    suspend fun crearMascota(
        @Path("usuarioId") usuarioId: Long,
        @Body mascota: Mascota
    ): Mascota


    @DELETE("api/mascotas/{id}")
    suspend fun eliminarMascota(@Path("id") id: Long)

}