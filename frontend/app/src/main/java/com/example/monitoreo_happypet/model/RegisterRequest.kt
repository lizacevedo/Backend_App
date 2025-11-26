package com.example.monitoreo_happypet.model

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val contraseña: String
)