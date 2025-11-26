package com.example.monitoreo_happypet.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.monitoreo_happypet.model.mascotas.Mascota
import com.example.monitoreo_happypet.remote.ApiService
import com.example.monitoreo_happypet.remote.RetrofitClient
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroMascotaScreen(
    userId: Long,
    onMascotaRegistrada: () -> Unit,
    onCancel: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }

    // Edad: valor + unidad
    var edadTexto by remember { mutableStateOf("") }
    var edadUnidad by remember { mutableStateOf("Años") }
    val unidadesEdad = listOf("Años", "Meses")
    var edadMenuAbierto by remember { mutableStateOf(false) }

    var peso by remember { mutableStateOf("") }

    // ▶️ NUEVO: lista desplegable para especie
    val especies = listOf("Perro", "Gato", "Hámster", "Conejo", "´Pez", "Pájaro", "Hurón", "Cobaya","Otro")
    var especieMenuAbierto by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val api = remember {
        RetrofitClient
            .getInstance(context)
            .create(ApiService::class.java)
    }

    val habilitado = nombre.isNotBlank() && !isSaving

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Registrar Mascota",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (habilitado) {
                        val edadNumero = edadTexto.toIntOrNull()
                        val edadEnAnios = edadNumero?.let {
                            if (edadUnidad == "Años") it
                            else max(1, it / 12)   // meses → años aprox.
                        }

                        val nuevaMascota = Mascota(
                            id = null,
                            nombre = nombre,
                            especie = especie.ifBlank { null },
                            raza = raza.ifBlank { null },
                            edad = edadEnAnios,
                            peso = peso.toDoubleOrNull(),
                            videoAsignado = null
                        )

                        errorMessage = null
                        isSaving = true

                        scope.launch {
                            try {
                                api.crearMascota(userId, nuevaMascota)
                                isSaving = false
                                onMascotaRegistrada()
                            } catch (e: Exception) {
                                isSaving = false
                                errorMessage = e.message ?: "Error al registrar la mascota"
                            }
                        }
                    }
                },
                containerColor = if (habilitado)
                    Color(0xFF6366F1)
                else
                    Color(0xFF6366F1).copy(alpha = 0.4f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Guardar mascota",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF312E81),
                            Color(0xFF6D28D9),
                            Color(0xFFEC4899)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.10f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Pets,
                                contentDescription = "Mascota",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Nueva Mascota",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontSize = 22.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ingresa los datos para iniciar su monitoreo 🐾",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                        )

                        // -------- NOMBRE --------
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = Color(0xFFBFDBFE)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                focusedBorderColor = Color(0xFF93C5FD),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = Color(0xFFBFDBFE),
                                focusedLabelColor = Color(0xFFBFDBFE),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedLeadingIconColor = Color(0xFFBFDBFE),
                                unfocusedLeadingIconColor = Color(0xFFBFDBFE)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // -------- ESPECIE (DESPLEGABLE) --------
                        // --- arriba, en los estados ---
// val especies = listOf("Perro", "Gato", "Otro")
// var especieMenuAbierto by remember { mutableStateOf(false) }
// 👆 esos estados se quedan igual

// -------- ESPECIE (DESPLEGABLE) --------
                        ExposedDropdownMenuBox(
                            expanded = especieMenuAbierto,
                            onExpandedChange = { especieMenuAbierto = !especieMenuAbierto }
                        ) {
                            OutlinedTextField(
                                value = if (especie.isBlank()) "" else especie,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Especie") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Pets,
                                        contentDescription = null,
                                        tint = Color(0xFFC4B5FD)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = especieMenuAbierto
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()        // 👈 importante para que el menú se posicione bien
                                    .fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                    focusedBorderColor = Color(0xFFC4B5FD),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                    cursorColor = Color(0xFFC4B5FD),
                                    focusedLabelColor = Color(0xFFC4B5FD),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                    focusedLeadingIconColor = Color(0xFFC4B5FD),
                                    unfocusedLeadingIconColor = Color(0xFFC4B5FD)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = especieMenuAbierto,
                                onDismissRequest = { especieMenuAbierto = false }
                            ) {
                                especies.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            especie = opcion
                                            especieMenuAbierto = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // -------- RAZA --------
                        OutlinedTextField(
                            value = raza,
                            onValueChange = { raza = it },
                            label = { Text("Raza") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFF9A8D4)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                focusedBorderColor = Color(0xFFF9A8D4),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = Color(0xFFF9A8D4),
                                focusedLabelColor = Color(0xFFF9A8D4),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedLeadingIconColor = Color(0xFFF9A8D4),
                                unfocusedLeadingIconColor = Color(0xFFF9A8D4)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // -------- EDAD + UNIDAD --------
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // ------ CAMPO NUMÉRICO DE EDAD ------
                            OutlinedTextField(
                                value = edadTexto,
                                onValueChange = { edadTexto = it },
                                label = { Text("Edad") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFFBBF7D0)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                    focusedBorderColor = Color(0xFF22C55E),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                    cursorColor = Color(0xFF22C55E),
                                    focusedLabelColor = Color(0xFF22C55E),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                    focusedLeadingIconColor = Color(0xFF22C55E),
                                    unfocusedLeadingIconColor = Color(0xFF22C55E)
                                )
                            )

                            // ----------- SELECTOR AÑOS / MESES -----------
                            ExposedDropdownMenuBox(
                                expanded = edadMenuAbierto,
                                onExpandedChange = { edadMenuAbierto = !edadMenuAbierto }
                            ) {

                                OutlinedTextField(
                                    value = edadUnidad,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unidad") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = edadMenuAbierto)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .width(120.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                        cursorColor = Color.White,
                                        focusedLabelColor = Color.White,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = edadMenuAbierto,
                                    onDismissRequest = { edadMenuAbierto = false }
                                ) {
                                    unidadesEdad.forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion) },
                                            onClick = {
                                                edadUnidad = opcion
                                                edadMenuAbierto = false
                                            }
                                        )
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))

                        // -------- PESO --------
                        OutlinedTextField(
                            value = peso,
                            onValueChange = { peso = it },
                            label = { Text("Peso (Kg)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.FitnessCenter,
                                    contentDescription = null,
                                    tint = Color(0xFFFB7185)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                focusedBorderColor = Color(0xFFFB7185),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
                                cursorColor = Color(0xFFFB7185),
                                focusedLabelColor = Color(0xFFFB7185),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedLeadingIconColor = Color(0xFFFB7185),
                                unfocusedLeadingIconColor = Color(0xFFFB7185)
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "💡 Solo el nombre es obligatorio. Puedes completar los demás datos más adelante.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f),
                            textAlign = TextAlign.Center
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "❌ $errorMessage",
                                color = Color(0xFFFCA5A5),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
