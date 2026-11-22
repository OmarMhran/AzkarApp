package com.example.azkarapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt


data class GoldPalette(
    val isDark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val card: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val text: Color,
    val muted: Color
)

private val DarkGold = GoldPalette(
    isDark = true,
    bgTop = Color(0xFF0D1613),
    bgBottom = Color(0xFF060A08),
    card = Color(0xFF131C18),
    border = Color(0xFFE5C158).copy(alpha = 0.28f),
    accent = Color(0xFFE5C158),
    onAccent = Color(0xFF1B1500),
    text = Color(0xFFF3EBD2),
    muted = Color(0xFFB9B29A)
)

private val LightGold = GoldPalette(
    isDark = false,
    bgTop = Color(0xFFFBF6E8),
    bgBottom = Color(0xFFF3ECD6),
    card = Color(0xFFFFFCF2),
    border = Color(0xFF8A6A1F).copy(alpha = 0.32f),
    accent = Color(0xFF8A6A1F),
    onAccent = Color(0xFFFFFFFF),
    text = Color(0xFF2A2415),
    muted = Color(0xFF6B6247)
)

@Composable
fun goldPalette(): GoldPalette =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) DarkGold else LightGold

/** نجمة ثمانية (مربعين متداخلين، واحد مايل 45°) مكررة كبلاط بخطوط رفيعة شفافة. */
fun Modifier.starPattern(color: Color, tile: Dp = 140.dp): Modifier = drawBehind {
    val tilePx = tile.toPx()
    val stroke = Stroke(width = 1.dp.toPx())
    val r = tilePx / 2f * 0.62f
    val d = r * sqrt(2f)
    var cy = 0f
    while (cy < size.height + tilePx) {
        var cx = 0f
        while (cx < size.width + tilePx) {
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
            cx += tilePx
        }
        cy += tilePx
    }
}