package com.example.azkarapp.presentation.main_hub.settings_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val FontScales = listOf(
    16f to 14.sp,
    20f to 16.sp,
    24f to 19.sp,
    28f to 22.sp,
    32f to 25.sp
)

@Composable
internal fun FontSizeCard(
    savedSize: Float,
    onSizeChangeFinished: (Float) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val selected = FontScales.minBy { abs(it.first - savedSize) }.first

    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SettingsCardMinHeight)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsIconBadge(icon = Icons.Rounded.TextFields, active = true)
                Text(
                    text = "حجم الخط",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceVariant.copy(alpha = 0.6f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FontScales.forEach { (value, glyphSize) ->
                    val isSelected = value == selected
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) colors.primary.copy(alpha = 0.18f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { if (!isSelected) onSizeChangeFinished(value) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "أ",
                            fontSize = glyphSize,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) colors.primary
                            else colors.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}