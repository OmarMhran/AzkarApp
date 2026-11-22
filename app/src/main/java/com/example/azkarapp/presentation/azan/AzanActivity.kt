package com.example.azkarapp.presentation.azan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.azkarapp.MainActivity
import com.example.azkarapp.util.notification.AzanService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

private val NightTop = Color(0xFF0D1613)
private val NightBottom = Color(0xFF040706)
private val Gold = Color(0xFFE5C158)
private val Ivory = Color(0xFFF3EBD2)
private val Muted = Color(0xFFB9B29A)


class AzanActivity : ComponentActivity() {

    private var azanEnded by mutableStateOf(false)

    private val endedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            azanEnded = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        ContextCompat.registerReceiver(
            this,
            endedReceiver,
            IntentFilter(AzanService.ACTION_AZAN_ENDED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "الصلاة"

        setContent {
            AzanScreen(
                prayerName = prayerName,
                ended = azanEnded,
                onStop = {
                    sendBroadcast(Intent(AzanService.ACTION_STOP_AZAN).setPackage(packageName))
                    finish()
                },
                onOpenApp = {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    finish()
                },
                onClose = { finish() }
            )
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(endedReceiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "PRAYER_NAME"
    }
}

@Composable
private fun AzanScreen(
    prayerName: String,
    ended: Boolean,
    onStop: () -> Unit,
    onOpenApp: () -> Unit,
    onClose: () -> Unit
) {
    val locale = remember { Locale("ar") }
    val time = remember { SimpleDateFormat("hh:mm", locale).format(Date()) }
    val date = remember { SimpleDateFormat("EEEE، d MMMM", locale).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightTop, NightBottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawStarPattern() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 900f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = "حيّ على الصلاة",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "أذان $prayerName",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Gold
                )
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 2.dp)
                        .background(Gold.copy(alpha = 0.6f), RoundedCornerShape(1.dp))
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .border(1.dp, Gold.copy(alpha = 0.35f), CircleShape)
                        .padding(10.dp)
                        .border(1.dp, Gold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Ivory
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = date, fontSize = 15.sp, color = Muted)
                if (ended) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "انتهى الأذان", fontSize = 14.sp, color = Gold)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = if (ended) onClose else onStop,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Color(0xFF1B1500)
                    )
                ) {
                    Text(
                        text = if (ended) "إغلاق" else "إيقاف الأذان",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onOpenApp,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.45f))
                ) {
                    Text(text = "فتح التطبيق", fontSize = 16.sp, color = Gold)
                }
            }
        }
    }
}

private fun DrawScope.drawStarPattern() {
    val tile = 140.dp.toPx()
    val half = tile / 2f
    val stroke = Stroke(width = 1.dp.toPx())
    val color = Gold.copy(alpha = 0.07f)
    val r = half * 0.62f
    val d = r * sqrt(2f)

    var cy = 0f
    while (cy < size.height + tile) {
        var cx = 0f
        while (cx < size.width + tile) {
            val square = Path().apply {
                moveTo(cx - r, cy - r); lineTo(cx + r, cy - r)
                lineTo(cx + r, cy + r); lineTo(cx - r, cy + r); close()
            }
            val diamond = Path().apply {
                moveTo(cx, cy - d); lineTo(cx + d, cy)
                lineTo(cx, cy + d); lineTo(cx - d, cy); close()
            }
            drawPath(square, color, style = stroke)
            drawPath(diamond, color, style = stroke)
            cx += tile
        }
        cy += tile
    }
}