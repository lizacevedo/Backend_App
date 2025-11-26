package com.example.monitoreo_happypet.model.mascotas

import com.google.gson.annotations.SerializedName


data class Mascota(
    val id: Long?,
    val nombre: String,
    val especie: String?,
    val raza: String?,
    val edad: Int?,
    val peso: Double?,
    @SerializedName("videoAsignado") // ← o simplemente elimina esta línea
    val videoAsignado: String?,     // vendrá "video2", "video3", etc.


)