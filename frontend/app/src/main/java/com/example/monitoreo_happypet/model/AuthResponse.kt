package com.example.monitoreo_happypet.model



data class AuthResponse(
    val mensaje: String,
    val token: String,
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val provider: String,

    )