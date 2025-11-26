package com.example.monitoreo_happypet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.monitoreo_happypet.remote.ApiService
import com.example.monitoreo_happypet.remote.RetrofitClient
import com.example.monitoreo_happypet.ui.theme.AuthViewModel
import com.example.monitoreo_happypet.ui.theme.RegistroMascotaScreen
import com.example.monitoreo_happypet.ui.theme.screens.ForgotScreen
import com.example.monitoreo_happypet.ui.theme.screens.LoginScreen
import com.example.monitoreo_happypet.ui.theme.screens.MascotasScreen
import com.example.monitoreo_happypet.ui.theme.screens.MonitoreoScreen
import com.example.monitoreo_happypet.ui.theme.screens.RegisterScreen
import com.example.monitoreo_happypet.ui.theme.screens.WelcomeScreen
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.example.monitoreo_happypet.R   // para R.raw.video1, etc.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val nav = rememberNavController()
                val vm: AuthViewModel = viewModel()

                NavHost(
                    navController = nav,
                    startDestination = "login"
                ) {

                    // ---------- LOGIN ----------
                    composable("login") {
                        LoginScreen(
                            vm = vm,
                            onForgot = { nav.navigate("forgot") },
                            onLogged = { userId, nombre ->
                                val safeNombre = URLEncoder.encode(
                                    nombre ?: "Usuario",
                                    StandardCharsets.UTF_8.toString()
                                )

                                vm.viewModelScope.launch {
                                    try {
                                        val api = RetrofitClient
                                            .getInstance(this@MainActivity)
                                            .create(ApiService::class.java)

                                        val mascotas = api.obtenerMascotas(userId)

                                        if (mascotas.isEmpty()) {
                                            // No tiene mascotas → bienvenida
                                            nav.navigate("welcome/$userId/$safeNombre") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            // Ya tiene → home
                                            nav.navigate("home/$userId") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Si falla, igual lo mandamos al home
                                        nav.navigate("home/$userId") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            onRegister = { nav.navigate("register") }
                        )
                    }

                    // ---------- REGISTRO USUARIO ----------
                    composable("register") {
                        RegisterScreen(
                            onBack = { nav.popBackStack() }
                        )
                    }

                    // ---------- OLVIDÓ CONTRASEÑA ----------
                    composable("forgot") {
                        ForgotScreen(
                            onBack = { nav.popBackStack() },
                            vm = vm
                        )
                    }

                    // ---------- BIENVENIDA ----------
                    composable(
                        route = "welcome/{userId}/{nombre}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.LongType },
                            navArgument("nombre") { type = NavType.StringType }
                        )
                    ) { backStack ->
                        val userId = backStack.arguments?.getLong("userId") ?: 0L
                        val nombre = backStack.arguments?.getString("nombre") ?: "Usuario"

                        WelcomeScreen(
                            userName = nombre,
                            onContinue = {
                                nav.navigate("registro-mascota/$userId")
                            }
                        )
                    }

                    // ---------- REGISTRO MASCOTA ----------
                    composable(
                        route = "registro-mascota/{userId}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.LongType }
                        )
                    ) { backStack ->
                        val userId = backStack.arguments?.getLong("userId") ?: 0L

                        RegistroMascotaScreen(
                            userId = userId,
                            onMascotaRegistrada = {
                                nav.navigate("home/$userId") {
                                    popUpTo("welcome") { inclusive = false }
                                }
                            },
                            onCancel = {
                                nav.navigate("home/$userId") {
                                    popUpTo("welcome") { inclusive = false }
                                }
                            }
                        )
                    }

                    // ---------- HOME (LISTA DE MASCOTAS) ----------
                    composable(
                        route = "home/{userId}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.LongType }
                        )
                    ) { backStack ->
                        val userId = backStack.arguments?.getLong("userId") ?: 0L

                        MascotasScreen(
                            userId = userId,
                            onAgregarMascota = {
                                nav.navigate("registro-mascota/$userId")
                            },
                            onVerCamara = { mascota ->
                                // 🔑 viene de la BD: "video1", "video2", "video3"
                                val rawKey = mascota.videoAsignado ?: "video1"

                                val normalizedKey = rawKey
                                    .trim()
                                    .lowercase()
                                    .replace("í", "i")
                                    .replace("ó", "o")
                                    .replace("á", "a")
                                    .replace("é", "e")
                                    .replace("ú", "u")
                                    .replace(" ", "")

                                val videoResId = when (normalizedKey) {
                                    "video1" -> R.raw.video1
                                    "video2" -> R.raw.video2
                                    "video3" -> R.raw.video3
                                    else     -> R.raw.video1
                                }

                                nav.navigate("monitoreo/$videoResId")
                            },
                            onLogout = {
                                vm.logout()
                                nav.navigate("login") {
                                    popUpTo(nav.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    // ---------- MONITOREO (usa recurso raw Int) ----------
                    composable(
                        route = "monitoreo/{videoResId}",
                        arguments = listOf(
                            navArgument("videoResId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val videoResId =
                            backStackEntry.arguments?.getInt("videoResId") ?: R.raw.video1

                        MonitoreoScreen(
                            videoResId = videoResId,
                            onBack = { nav.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
