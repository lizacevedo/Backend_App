// MascotasViewModel.kt
package com.example.monitoreo_happypet.ui.theme

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitoreo_happypet.model.mascotas.Mascota
import com.example.monitoreo_happypet.remote.ApiService
import com.example.monitoreo_happypet.remote.RetrofitClient
import kotlinx.coroutines.launch

class MascotasViewModel(app: Application) : AndroidViewModel(app) {

    private val api = RetrofitClient.getInstance(app).create(ApiService::class.java)

    var mascotas by mutableStateOf<List<Mascota>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun cargarMascotas(userId: Long) {
        viewModelScope.launch {
            try {
                cargando = true
                error = null
                mascotas = api.obtenerMascotas(userId)   // ← aquí llegan las de Postman
            } catch (e: Exception) {
                error = e.message ?: "Error al obtener mascotas"
            } finally {
                cargando = false
            }
        }
    }
}
