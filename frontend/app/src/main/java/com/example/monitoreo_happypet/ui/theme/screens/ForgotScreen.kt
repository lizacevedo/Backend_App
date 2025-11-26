package com.example.monitoreo_happypet.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitoreo_happypet.ui.theme.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotScreen(
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var correo by remember { mutableStateOf("") }
    var nuevaContraseña by remember { mutableStateOf("") }
    var confirmarContraseña by remember { mutableStateOf("") }
    var pasoActual by remember { mutableStateOf(1) } // 1: Correo, 2: Nueva contraseña

    val ui by vm.ui.collectAsState()
    val scrollState = rememberScrollState()

    // Si la operación fue exitosa, mostrar mensaje de éxito
    if (ui.operacionExitosa) {
        LaunchedEffect(Unit) {
            // Regresar automáticamente después de 2 segundos
            kotlinx.coroutines.delay(2000)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Recuperar Contraseña",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Indicador de pasos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(step = 1, currentStep = pasoActual, label = "Correo")
                Spacer(modifier = Modifier.width(16.dp))
                StepIndicator(step = 2, currentStep = pasoActual, label = "Nueva Contraseña")
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (pasoActual) {
                1 -> {
                    // PASO 1: Ingresar correo
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Correo",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Ingresa tu correo",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Introduce el correo asociado a tu cuenta para recuperar tu contraseña",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo electrónico") },
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = "Correo")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = ui.mensaje?.contains("Error") == true
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (correo.isNotBlank()) {
                                    pasoActual = 2
                                    vm.limpiarEstado() // Limpiar mensajes anteriores
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = correo.isNotBlank() && !ui.cargando,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (ui.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Continuar", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                2 -> {
                    // PASO 2: Nueva contraseña
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Contraseña",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Nueva Contraseña",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Crea una nueva contraseña segura para tu cuenta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = nuevaContraseña,
                            onValueChange = { nuevaContraseña = it },
                            label = { Text("Nueva Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = "Contraseña")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = nuevaContraseña.isNotEmpty() && nuevaContraseña.length < 6
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmarContraseña,
                            onValueChange = { confirmarContraseña = it },
                            label = { Text("Confirmar Contraseña") },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = "Confirmar")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = confirmarContraseña.isNotEmpty() && nuevaContraseña != confirmarContraseña
                        )

                        // Mensajes de validación
                        if (nuevaContraseña.isNotEmpty() && nuevaContraseña.length < 6) {
                            Text(
                                text = "La contraseña debe tener al menos 6 caracteres",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        if (confirmarContraseña.isNotEmpty() && nuevaContraseña != confirmarContraseña) {
                            Text(
                                text = "Las contraseñas no coinciden",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (nuevaContraseña == confirmarContraseña && nuevaContraseña.length >= 6) {
                                    vm.actualizarContraseña(correo, nuevaContraseña)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = nuevaContraseña.isNotBlank() &&
                                    confirmarContraseña.isNotBlank() &&
                                    nuevaContraseña == confirmarContraseña &&
                                    nuevaContraseña.length >= 6 &&
                                    !ui.cargando,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (ui.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Actualizar Contraseña", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(onClick = { pasoActual = 1 }) {
                            Text("Volver atrás")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de estado
            if (ui.mensaje != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            ui.operacionExitosa -> MaterialTheme.colorScheme.primaryContainer
                            ui.mensaje!!.contains("❌") -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = ui.mensaje!!,
                        modifier = Modifier.padding(16.dp),
                        color = when {
                            ui.operacionExitosa -> MaterialTheme.colorScheme.onPrimaryContainer
                            ui.mensaje!!.contains("❌") -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (step <= currentStep) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Text(
                text = step.toString(),
                color = if (step <= currentStep) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (step <= currentStep) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}