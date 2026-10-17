package com.example.azkarapp.presentation.main_hub.compass_screen


import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback


data class QiblaUiState(
    val isLoading: Boolean = true,
    val azimuth: Float = 0f,
    val qiblahBearing: Float = 0f,
    val isAlignedWithQiblah: Boolean = false,
    val cityName: String = "",
    val hasCompassSensor: Boolean = true,
    val errorMessage: String? = null
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblahCompassScreen(
    viewModel: QiblaViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.isAlignedWithQiblah) {
        if (uiState.isAlignedWithQiblah) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = -uiState.azimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CompassRotation"
    )

    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val goldColor = Color(0xFFD4AF37)
    val darkGold = Color(0xFF996515)
    val alignedGreen = Color(0xFF10B981)
    val isAligned = uiState.isAlignedWithQiblah

    val mainAccentColor by animateColorAsState(
        targetValue = if (isAligned) alignedGreen else goldColor,
        animationSpec = tween(500),
        label = "AccentColor"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isAligned) 0.35f else 0.18f,
        animationSpec = tween(500),
        label = "GlowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اتجاه القبلة", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = goldColor)
                }
            }
            !uiState.hasCompassSensor -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("جهازك لا يحتوي على حساس البوصلة", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(mainAccentColor.copy(alpha = glowAlpha), Color.Transparent)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Surface(
                            shape = RoundedCornerShape(25.dp),
                            color = if (isAligned) alignedGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, mainAccentColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (isAligned) "أنت الآن باتجاه القبلة الصحيح 🕋" else "حاذِ السهم الذهبي مع المؤشر العلوي",
                                fontSize = 14.sp,
                                color = if (isAligned) alignedGreen else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(310.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                                    .size(12.dp, 20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(mainAccentColor, mainAccentColor.copy(alpha = 0.3f))
                                        )
                                    )
                            )

                            Canvas(
                                modifier = Modifier
                                    .size(280.dp)
                                    .rotate(animatedAzimuth)
                            ) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val outerRadius = size.width / 2
                                val innerRadius = outerRadius - 20.dp.toPx()

                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(goldColor.copy(alpha = 0.6f), darkGold.copy(alpha = 0.2f), goldColor.copy(alpha = 0.6f))
                                    ),
                                    radius = outerRadius,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    radius = innerRadius
                                )

                                val directions = mapOf(0 to "ش", 90 to "ق", 180 to "ج", 270 to "غ")

                                for (i in 0 until 360 step 5) {
                                    val angleRad = Math.toRadians(i.toDouble())
                                    val isMajor = i % 30 == 0
                                    val isCardinal = i % 90 == 0

                                    val tickLength = if (isCardinal) 14.dp.toPx() else if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                                    val color = if (isCardinal) mainAccentColor else if (isMajor) Color.Gray else Color.Gray.copy(alpha = 0.4f)

                                    val lineStart = Offset(
                                        x = center.x + (innerRadius - tickLength) * sin(angleRad).toFloat(),
                                        y = center.y - (innerRadius - tickLength) * cos(angleRad).toFloat()
                                    )
                                    val lineEnd = Offset(
                                        x = center.x + innerRadius * sin(angleRad).toFloat(),
                                        y = center.y - innerRadius * cos(angleRad).toFloat()
                                    )

                                    drawLine(
                                        color = color,
                                        start = lineStart,
                                        end = lineEnd,
                                        strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                                    )

                                    if (isCardinal && directions.containsKey(i)) {
                                        val textRadius = innerRadius - 28.dp.toPx()
                                        val textX = center.x + textRadius * sin(angleRad).toFloat()
                                        val textY = center.y - textRadius * cos(angleRad).toFloat() + 6.dp.toPx()

                                        drawContext.canvas.nativeCanvas.drawText(
                                            directions[i]!!,
                                            textX,
                                            textY,
                                            android.graphics.Paint().apply {
                                                setColor(if (i == 0) android.graphics.Color.RED else labelColor)
                                                textSize = 36f
                                                textAlign = android.graphics.Paint.Align.CENTER
                                                isFakeBoldText = true
                                            }
                                        )
                                    }
                                }

                                val qiblahRad = Math.toRadians(uiState.qiblahBearing.toDouble())
                                val arrowLength = innerRadius - 40.dp.toPx()

                                val tip = Offset(
                                    x = center.x + arrowLength * sin(qiblahRad).toFloat(),
                                    y = center.y - arrowLength * cos(qiblahRad).toFloat()
                                )

                                val leftWing = Offset(
                                    x = center.x + 16.dp.toPx() * sin(qiblahRad - Math.toRadians(140.0)).toFloat(),
                                    y = center.y - 16.dp.toPx() * cos(qiblahRad - Math.toRadians(140.0)).toFloat()
                                )

                                val rightWing = Offset(
                                    x = center.x + 16.dp.toPx() * sin(qiblahRad + Math.toRadians(140.0)).toFloat(),
                                    y = center.y - 16.dp.toPx() * cos(qiblahRad + Math.toRadians(140.0)).toFloat()
                                )

                                val arrowPath = Path().apply {
                                    moveTo(tip.x, tip.y)
                                    lineTo(leftWing.x, leftWing.y)
                                    lineTo(center.x, center.y)
                                    lineTo(rightWing.x, rightWing.y)
                                    close()
                                }

                                drawPath(
                                    path = arrowPath,
                                    brush = Brush.linearGradient(
                                        colors = listOf(mainAccentColor, goldColor.copy(alpha = 0.7f)),
                                        start = center,
                                        end = tip
                                    )
                                )

                                drawCircle(
                                    color = Color.Black,
                                    radius = 12.dp.toPx(),
                                    center = center
                                )
                                drawCircle(
                                    color = mainAccentColor,
                                    radius = 12.dp.toPx(),
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الموقع الحقيقي", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = uiState.cityName.ifEmpty { "جارٍ التحميل..." },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                VerticalDivider(modifier = Modifier.height(30.dp), color = Color.Gray.copy(alpha = 0.3f))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("زاوية القبلة", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = "${uiState.qiblahBearing.toInt()}° North",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = mainAccentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}