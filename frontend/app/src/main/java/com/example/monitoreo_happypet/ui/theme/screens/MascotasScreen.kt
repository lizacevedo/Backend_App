package com.example.monitoreo_happypet.ui.theme.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitoreo_happypet.model.mascotas.Mascota
import com.example.monitoreo_happypet.remote.ApiService
import com.example.monitoreo_happypet.remote.RetrofitClient
import com.example.monitoreo_happypet.ui.theme.MascotasViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotasScreen(
    userId: Long,
    onAgregarMascota: () -> Unit,
    onVerCamara: (Mascota) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MascotasViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )

    val scope = rememberCoroutineScope()

    val api = remember {
        RetrofitClient.getInstance(context).create(ApiService::class.java)
    }

    var refrescar by remember { mutableStateOf(false) }

    LaunchedEffect(userId, refrescar) {
        viewModel.cargarMascotas(userId)
    }

    val mascotas = viewModel.mascotas
    val cargando = viewModel.cargando
    val error = viewModel.error

    var mascotaSeleccionada by remember { mutableStateOf<Mascota?>(null) }
    var showDetalle by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        Color(0xFF111827),
                        Color(0xFF1D2345),
                        Color(0xFF7C3AED)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // ---------- HEADER CON CERRAR SESIÓN ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HappyPet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFBFDBFE)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tus Mascotas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Administra y monitorea a tus compañeros peludos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = Color(0xFFBFDBFE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!cargando && error == null && mascotas.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.06f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4F46E5),
                                Color(0xFFEC4899)
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFF1D4ED8),
                                            Color(0xFF6366F1)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tienes ${mascotas.size} mascot${if (mascotas.size == 1) "a" else "as"} registrad${if (mascotas.size == 1) "a" else "as"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Toca una tarjeta para ver detalles o usar opciones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF93C5FD))
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFB91C1C).copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "❌ $error",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                mascotas.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF4F46E5),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Pets,
                                contentDescription = "Sin mascotas",
                                tint = Color(0xFFBFDBFE),
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Aún no tienes mascotas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Comienza agregando a tu primer compañero peludo.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onAgregarMascota,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Agregar mi primera mascota")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mascotas) { mascota ->
                            MascotaCard(
                                mascota = mascota,
                                onClick = {
                                    mascotaSeleccionada = mascota
                                    showDetalle = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (mascotas.isNotEmpty()) {
                                onVerCamara(mascotas.first())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D4ED8),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Monitoreo en tiempo real")
                    }
                }
            }

            if (!cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            onAgregarMascota()
                            refrescar = !refrescar
                        },
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    ) {
                        Text("+")
                    }
                }
            }
        }

        // ---------- DIALOGO DETALLE + ELIMINAR ----------
        if (showDetalle && mascotaSeleccionada != null) {
            MascotaDetalleDialog(
                mascota = mascotaSeleccionada!!,
                onCerrar = { showDetalle = false },
                onEliminar = {
                    val id = mascotaSeleccionada!!.id
                    if (id != null) {
                        scope.launch {
                            try {
                                api.eliminarMascota(id)
                                showDetalle = false
                                refrescar = !refrescar
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            )
        }
    }
}

/* ---------- CARD DE MASCOTA ---------- */

@Composable
fun MascotaCard(
    mascota: Mascota,
    onClick: () -> Unit
) {
    val emoji = when ((mascota.especie ?: "").lowercase()) {
        "perro" -> "🐶"
        "gato" -> "🐱"
        "conejo" -> "🐰"
        "ave", "pájaro", "pajaro" -> "🐦"
        else -> "🐾"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF6366F1).copy(alpha = 0.7f),
                    Color(0xFFEC4899).copy(alpha = 0.6f)
                )
            )
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF4F46E5),
                                Color(0xFFEC4899)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mascota.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!mascota.especie.isNullOrBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(mascota.especie!!, color = Color(0xFF60A5FA)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                    if (!mascota.raza.isNullOrBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { Text(mascota.raza!!, color = Color(0xFFF973AB)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = Color(0xFFF973AB)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                if (mascota.edad != null || mascota.peso != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (mascota.edad != null) append("${mascota.edad} años")
                            if (mascota.peso != null) {
                                if (mascota.edad != null) append(" • ")
                                append("${mascota.peso} kg")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Toca para consultar más datos",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/* ---------- DIALOGO DETALLE VETERINARIO ---------- */

/* ---------- DIALOGO DETALLE VETERINARIO MEJORADO ---------- */

@Composable
fun MascotaDetalleDialog(
    mascota: Mascota,
    onCerrar: () -> Unit,
    onEliminar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFF0F172A).copy(alpha = 0.98f),
        tonalElevation = 16.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFFEC4899)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = when ((mascota.especie ?: "").lowercase()) {
                        "perro" -> "🐶"
                        "gato" -> "🐱"
                        "conejo" -> "🐰"
                        "ave", "pájaro", "pajaro" -> "🐦"
                        else -> "🐾"
                    }
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = mascota.nombre,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Ficha veterinaria completa",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // INFORMACIÓN BÁSICA
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF334155), Color(0xFF475569))
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "📋 Información General",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF60A5FA)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFF059669).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoItem(
                                title = "Especie",
                                value = mascota.especie ?: "No especificada",
                                icon = "🐕",
                                color = Color(0xFF60A5FA)
                            )
                            InfoItem(
                                title = "Raza",
                                value = mascota.raza ?: "Mixta",
                                icon = "🧬",
                                color = Color(0xFFF973AB)
                            )
                        }
                    }
                }

                // ESTADÍSTICAS VITALES
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📊 Estadísticas Vitales",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF60A5FA),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VitalStatCard(
                                title = "Edad",
                                value = "${mascota.edad ?: "?"} años",
                                icon = "🎂",
                                colorStart = Color(0xFF8B5CF6),
                                colorEnd = Color(0xFFEC4899)
                            )

                            VitalStatCard(
                                title = "Peso",
                                value = "${mascota.peso ?: "?"} kg",
                                icon = "⚖️",
                                colorStart = Color(0xFF10B981),
                                colorEnd = Color(0xFF059669)
                            )

                            VitalStatCard(
                                title = "Estado",
                                value = "Saludable",
                                icon = "❤️",
                                colorStart = Color(0xFFEF4444),
                                colorEnd = Color(0xFFDC2626)
                            )
                        }
                    }
                }

                // RECOMENDACIONES VETERINARIAS (SIMULADAS)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "🏥 Recomendaciones Veterinarias",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF60A5FA),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        RecomendacionItem(
                            icon = "💉",
                            title = "Vacunación",
                            description = "Próxima dosis en 2 meses",
                            statusColor = Color(0xFF10B981)
                        )

                        RecomendacionItem(
                            icon = "🍖",
                            title = "Alimentación",
                            description = "Dieta balanceada recomendada",
                            statusColor = Color(0xFFF59E0B)
                        )

                        RecomendacionItem(
                            icon = "🏃",
                            title = "Ejercicio",
                            description = "30 min de actividad diaria",
                            statusColor = Color(0xFF3B82F6)
                        )
                    }
                }

                // ULTIMO CONTROL (SIMULADO)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1B4B)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(0xFF4C1D95),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📅", style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Último control veterinario",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                "Hace 2 semanas • Todo normal",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "OK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onEliminar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onCerrar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    )
}

@Composable
private fun InfoItem(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier  // ✅ Agregar modifier como parámetro
) {
    Column(modifier = modifier) {  // ✅ Usar el modifier aquí
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF94A3B8)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White
        )
    }
}

@Composable
private fun VitalStatCard(
    title: String,
    value: String,
    icon: String,
    colorStart: Color,
    colorEnd: Color,
    modifier: Modifier = Modifier  // ✅ Agregar modifier como parámetro
) {
    Card(
        modifier = modifier,  // ✅ Usar el modifier que viene de afuera
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(colorStart, colorEnd)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecomendacionItem(
    icon: String,
    title: String,
    description: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = statusColor, shape = CircleShape)
        )
    }
}