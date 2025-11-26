package com.example.monitoreo_happypet.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitoreo_happypet.data.TokenManager
import com.example.monitoreo_happypet.model.GoogleLoginRequest
import com.example.monitoreo_happypet.model.LoginRequest
import com.example.monitoreo_happypet.model.ResetPasswordRequest
import com.example.monitoreo_happypet.remote.ApiService
import com.example.monitoreo_happypet.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val token: String? = null,
    val nombre: String? = null,
    val logueado: Boolean = false,
    val userId: Long? = null,
    val operacionExitosa: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val api = RetrofitClient.getInstance(app).create(ApiService::class.java)
    private val tokenManager = TokenManager(app)

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    // 🔹 LOGIN NORMAL
    fun login(correo: String, contraseña: String) {
        viewModelScope.launch {
            try {
                _ui.value = UiState(cargando = true)

                val r = api.login(LoginRequest(correo, contraseña))
                tokenManager.saveToken(r.token)

                _ui.value = UiState(
                    cargando = false,
                    mensaje = r.mensaje,
                    token = r.token,
                    nombre = r.nombre,
                    logueado = true,
                    userId = r.id      // ✅ USAMOS EL ID REAL
                )

            } catch (e: Exception) {
                _ui.value = UiState(
                    cargando = false,
                    mensaje = "❌ Error: ${e.message}"
                )
            }
        }
    }

    // 🔹 LOGIN CON GOOGLE
    fun loginConGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _ui.value = UiState(cargando = true)

                val r = api.loginGoogle(GoogleLoginRequest(idToken))
                tokenManager.saveToken(r.token)

                _ui.value = UiState(
                    cargando = false,
                    mensaje = r.mensaje,
                    token = r.token,
                    nombre = r.nombre,
                    logueado = true,
                    userId = r.id    // ✅ TAMBIÉN USA EL ID REAL
                )

            } catch (e: Exception) {
                _ui.value = UiState(
                    cargando = false,
                    mensaje = "❌ Google: ${e.message}"
                )
            }
        }
    }

    // 🔹 Actualizar contraseña
    fun actualizarContraseña(correo: String, nuevaContraseña: String) {
        viewModelScope.launch {
            try {
                _ui.value = UiState(cargando = true)

                val request = mapOf(
                    "correo" to correo,
                    "nuevaContraseña" to nuevaContraseña
                )

                val response = api.actualizarContraseña(request)

                _ui.value = UiState(
                    cargando = false,
                    mensaje = "✅ ${response["mensaje"]}",
                    operacionExitosa = true
                )

            } catch (e: Exception) {
                _ui.value = UiState(
                    cargando = false,
                    mensaje = "❌ Error: ${e.message}",
                    operacionExitosa = false
                )
            }
        }
    }

    // 🔹 Recuperar contraseña
    fun resetPassword(correo: String) {
        viewModelScope.launch {
            try {
                _ui.value = UiState(cargando = true)

                api.resetPassword(ResetPasswordRequest(correo))

                _ui.value = UiState(
                    cargando = false,
                    mensaje = "📧 Te enviamos instrucciones a $correo"
                )

            } catch (e: Exception) {
                _ui.value = UiState(
                    cargando = false,
                    mensaje = "⚠️ Endpoint en construcción. Usa la nueva opción de cambio directo."
                )
            }
        }
    }

    // 🔹 Limpia SOLO el estado en memoria
    fun limpiarEstado() {
        _ui.value = UiState()
    }

    // 🔹 LOGOUT COMPLETO (memoria + token guardado)
    fun logout() {
        // 1. Resetear el estado de inmediato (para que Login no auto-navegue)
        _ui.value = UiState()  // logueado = false, userId = null, etc.

        // 2. Borrar el token en DataStore en segundo plano
        viewModelScope.launch {
            try {
                tokenManager.clear()
            } catch (_: Exception) {
                // si falla, no bloquees el logout
            }
        }
    }
}