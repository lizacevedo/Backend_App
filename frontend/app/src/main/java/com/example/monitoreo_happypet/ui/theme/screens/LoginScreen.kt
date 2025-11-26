package com.example.monitoreo_happypet.ui.theme.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitoreo_happypet.R
import com.example.monitoreo_happypet.ui.theme.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    vm: AuthViewModel = viewModel(),
    onForgot: () -> Unit,
    onLogged: (Long, String?) -> Unit,
    onRegister: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- Google Sign-In (igual que antes) ---
    val activity = LocalContext.current as Activity
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("TU_CLIENT_ID_DE_GOOGLE.apps.googleusercontent.com")
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(activity, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) vm.loginConGoogle(idToken)
        } catch (_: Exception) {}
    }

    // Navegar cuando esté logueado
    LaunchedEffect(ui.logueado, ui.userId) {
        if (ui.logueado && ui.userId != null) {
            onLogged(ui.userId!!, ui.nombre)
        }
    }

    // ------------------ UI ESTÉTICA A1 ------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),  // azul oscuro
                        Color(0xFF312E81),  // indigo
                        Color(0xFF6D28D9),  // morado
                        Color(0xFFEC4899)   // rosado
                    )
                )
            )
    ) {
        // Glow suave arriba
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(300f, 50f),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- LOGO + TITULO ----------
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.happypet_logo),
                    contentDescription = "HappyPet logo",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "HappyPet",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )

            Text(
                text = "Monitor en tiempo real para tus mejores amigos 🐶🐱",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

            // ---------- CARD GLASSMORPHISM ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Bienvenido de nuevo",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "Inicia sesión para seguir cuidando a tus peluditos 💜",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // ---------- CORREO (más redondeado) ----------
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = {
                            Text(
                                "Correo electrónico",
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = null,
                                tint = Color(0xFFBFDBFE)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,          // 👈 AÑADIR
                            unfocusedTextColor = Color.White,        // 👈 AÑADIR

                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF93C5FD),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            cursorColor = Color(0xFFBFDBFE),
                            focusedLabelColor = Color(0xFFBFDBFE),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                            focusedLeadingIconColor = Color(0xFFBFDBFE),
                            unfocusedLeadingIconColor = Color(0xFFBFDBFE)
                        )
                    )


                    Spacer(modifier = Modifier.height(14.dp))

                    // ---------- CONTRASEÑA (más redondeado) ----------
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = {
                            Text(
                                "Contraseña",
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFC4B5FD)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = "Ver contraseña",
                                    tint = Color.White
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContainerColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF93C5FD),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                            cursorColor = Color(0xFFBFDBFE),
                            focusedLabelColor = Color(0xFFBFDBFE),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.75f),
                            focusedLeadingIconColor = Color(0xFFBFDBFE),
                            unfocusedLeadingIconColor = Color(0xFFBFDBFE),
                        )


                    )

                    // Olvidé contraseña
                    TextButton(
                        onClick = onForgot,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            color = Color(0xFFBFDBFE),
                            style = MaterialTheme.typography.bodySmall

                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ---------- BOTÓN LOGIN ----------
                    val puedeIngresar =
                        !ui.cargando && correo.isNotBlank() && clave.isNotBlank()

                    Button(
                        onClick = { vm.login(correo.trim(), clave) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = puedeIngresar,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C83FF),              // 💜 más claro, más visible
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.18f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        if (ui.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Ingresar")
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- DIVIDER ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.25f)
                        )
                        Text(
                            " o continúa con ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.25f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---------- BOTÓN GOOGLE ----------
                    OutlinedButton(
                        onClick = { launcher.launch(googleClient.signInIntent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !ui.cargando,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFBFDBFE),
                                    Color(0xFFF9A8D4)
                                )
                            )
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continuar con Google")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- MENSAJES DE ESTADO ----------
                    ui.mensaje?.let { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.contains("❌"))
                                    Color(0xFFB91C1C).copy(alpha = 0.25f)
                                else
                                    Color(0xFF10B981).copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = msg,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ---------- ENLACE REGISTRO (más limpio y resaltado) ----------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "¿Aún no tienes cuenta? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        TextButton(onClick = onRegister) {
                            Text(
                                "Crear cuenta",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            }
        }
    }
}
