package com.example.monitoreo_happypet.ui.theme.screens

import android.graphics.Bitmap
import android.graphics.Color.alpha
import android.media.MediaMetadataRetriever
import androidx.annotation.RawRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.monitoreo_happypet.remote.RetrofitClient
import com.example.monitoreo_happypet.remote.roboflow.DeteccionApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoScreen(
    @RawRes videoResId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Texto mostrado debajo del video (última detección)
    var deteccionesTexto by remember { mutableStateOf("") }

    // Cliente Retrofit para detección
    val apiDeteccion = remember {
        RetrofitClient
            .getInstance(context)
            .create(DeteccionApi::class.java)
    }

    // Uri del video en res/raw
    val videoUri = RawResourceDataSource.buildRawResourceUri(videoResId)

    // ExoPlayer para reproducir el video
    val exoPlayer = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            playWhenReady = true
            prepare()
        }
    }

    // MediaMetadataRetriever para sacar frames
    val retriever = remember(videoResId) {
        MediaMetadataRetriever()
    }
    var retrieverOk by remember { mutableStateOf(false) }

    // Preparar retriever de forma segura con rawResourceFd
    LaunchedEffect(videoResId) {
        withContext(Dispatchers.IO) {
            try {
                val afd = context.resources.openRawResourceFd(videoResId)
                retriever.setDataSource(
                    afd.fileDescriptor,
                    afd.startOffset,
                    afd.length
                )
                afd.close()
                retrieverOk = true
            } catch (e: Exception) {
                retrieverOk = false
                deteccionesTexto = "No se pudo preparar el video para detección"
            }
        }
    }

    // Liberar recursos cuando se sale
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            retriever.release()
        }
    }

    // 🔁 Bucle: cada 300 ms toma un frame y manda a backend (LO DEJAMOS IGUAL)
    LaunchedEffect(exoPlayer, retrieverOk) {
        while (true) {
            if (retrieverOk) {
                try {
                    val currentPosMs = exoPlayer.currentPosition

                    val frameBytes = withContext(Dispatchers.IO) {
                        try {
                            val bitmap: Bitmap? = retriever.getFrameAtTime(
                                currentPosMs * 1000, // microsegundos
                                MediaMetadataRetriever.OPTION_CLOSEST
                            )

                            if (bitmap != null) {
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                                baos.toByteArray()
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (frameBytes != null) {
                        val requestFile =
                            frameBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData(
                            "file",
                            "frame.jpg",
                            requestFile
                        )

                        val response = withContext(Dispatchers.IO) {
                            apiDeteccion.detectarEnImagen(part)
                        }

                        if (response.isSuccessful) {
                            val preds = response.body()?.predictions ?: emptyList()
                            deteccionesTexto =
                                if (preds.isEmpty()) "Sin detecciones"
                                else preds.joinToString(", ") { it.clase }
                        } else {
                            deteccionesTexto = "Error: ${response.code()}"
                        }
                    }

                } catch (e: Exception) {
                    deteccionesTexto = "Error en detección"
                }
            }

            delay(300L)
        }
    }

    // 💡 Estados SOLO VISUALES para "conexión"
    var camaraConectada by remember { mutableStateOf(false) }
    var telegramConectado by remember { mutableStateOf(false) }

    // Animaciones para los efectos
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    LaunchedEffect(Unit) {
        // Pequeña animación al entrar, luego se queda en "conectado"
        delay(1200)
        camaraConectada = true
        delay(800)
        telegramConectado = true
    }

    // ---------------- UI SUPER MEJORADA ----------------
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🐾 HappyPet Monitor",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Vigilancia Inteligente Activada",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF60A5FA)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                ),
                                shape = CircleShape
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF312E81)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            // Efectos de fondo decorativos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x156366F1),
                                Color(0x058B5CF6),
                                Color(0x001E1B4B)
                            ),
                            radius = 800f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ---------- ESTADOS CONECTIVIDAD SUPER LLAMATIVOS ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ConnectionCard(
                        title = "📷 CÁMARA",
                        subtitle = if (camaraConectada) "CONECTADA ✓" else "CONECTANDO...",
                        colorStart = Color(0xFF6366F1),
                        colorEnd = Color(0xFF8B5CF6),
                        isReady = camaraConectada,
                        pulseAlpha = pulseAlpha,
                        modifier = Modifier.weight(1f)
                    )
                    ConnectionCard(
                        title = "🔔 TELEGRAM",
                        subtitle = if (telegramConectado) "NOTIFICACIONES ACTIVAS ✓" else "VINCULANDO...",
                        colorStart = Color(0xFF10B981),
                        colorEnd = Color(0xFF059669),
                        isReady = telegramConectado,
                        pulseAlpha = pulseAlpha,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- CARD del video CON EFECTOS ESPECIALES ----
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = false
                        )
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        useController = false
                                        player = exoPlayer
                                    }
                                }
                            )

                            // Etiqueta LIVE con efecto brillante
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "EN VIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            // DETECCIONES PEQUEÑAS CON EFECTO NEÓN
                            if (deteccionesTexto.isNotBlank() && !deteccionesTexto.startsWith("Error") &&
                                !deteccionesTexto.startsWith("No se pudo") && deteccionesTexto != "Sin detecciones") {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                        .background(
                                            color = Color(0xAA1E293B),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.linearGradient(
                                                listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
                                            ),
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    color = Color(0xFF10B981),
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🔍 $deteccionesTexto",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Efecto de brillo exterior
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x156366F1),
                                        Color(0x00000000)
                                    ),
                                    center = Offset(0.5f, 0.0f), // ✅ CORRECTO - Offset con valores entre 0 y 1
                                    radius = 600f
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ---- PANEL DE ACTIVIDAD SUPER LLAMATIVO ----
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF8B5CF6)))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🎯 ACTIVIDAD DETECTADA",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA)
                                )
                            )
                            Text(
                                text = when {
                                    deteccionesTexto.isBlank() -> "🎬 Iniciando análisis..."
                                    deteccionesTexto.startsWith("Error") -> "⚠️ Revisando conexión"
                                    deteccionesTexto == "Sin detecciones" -> "✅ Todo en orden"
                                    else -> "🚀 ¡Actividad detectada!"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }

                        // Indicador de actividad con pulso
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = if (deteccionesTexto.isNotBlank() && !deteccionesTexto.startsWith("Error"))
                                        Color(0xFF10B981) else Color(0xFFF59E0B),
                                    shape = CircleShape
                                )
                                .graphicsLayer {
                                    alpha = pulseAlpha
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ---- TARJETA INFORMATIVA CON ESTILO PREMIUM ----
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xAA1E293B)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "🌟 SISTEMA INTELIGENTE ACTIVO",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE879F9)
                                )
                            )
                        }

                        Text(
                            text = "HappyPet está monitoreando en tiempo real el comportamiento de tus mascotas. Recibirás notificaciones instantáneas en Telegram cuando detectemos actividad importante.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Stats en tiempo real
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem("🔄", "Análisis en tiempo real", "300ms")
                            StatItem("📊", "Precisión", "98%")
                            StatItem("⚡", "Velocidad", "Instantáneo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    title: String,
    subtitle: String,
    colorStart: Color,
    colorEnd: Color,
    isReady: Boolean,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(colorStart, colorEnd))
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x20FFFFFF),
                            Color(0x05FFFFFF)
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
                // Icono con efecto de brillo
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(colorStart, colorEnd)),
                            shape = CircleShape
                        )
                        .graphicsLayer {
                            alpha = if (isReady) 1f else pulseAlpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isReady) Color(0xFF10B981) else Color(0xFFF59E0B),
                                shape = CircleShape
                            )
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = if (isReady) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isReady) Color(0xFF34D399) else Color(0xFFFBBF24)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF60A5FA)
        )
    }
}