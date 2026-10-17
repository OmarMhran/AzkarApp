package com.example.azkarapp.presentation.main_hub.settings_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azkarapp.widget.WidgetConfig
import com.example.azkarapp.widget.WidgetStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsCards(
    config: WidgetConfig,
    onStyleSelected: (WidgetStyle) -> Unit,
    onFontScaleSelected: (Int) -> Unit,
    onOpacityChangeFinished: (Int) -> Unit
) {
    var sheetVisible by remember { mutableStateOf(false) }

    val fontLabel = WidgetStyle.FONT_SCALES
        .firstOrNull { it.first == config.fontScale }?.second ?: "وسط"

    SettingsSelectionCard(
        title = "إعدادات الـ Widget",
        value = "${config.style.label} · خط $fontLabel · شفافية ${config.opacity}%",
        contentDescription = "إعدادات الـ Widget",
        onClick = { sheetVisible = true }
    )

    if (sheetVisible) {
        // القيمة المؤقتة أثناء سحب الشفافية.
        var tempOpacity by remember(config.opacity) { mutableFloatStateOf(config.opacity.toFloat()) }

        ModalBottomSheet(
            onDismissRequest = { sheetVisible = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                SheetSectionTitle("الشكل")
                WidgetStyle.entries.forEach { option ->
                    val selected = option == config.style
                    ListItem(
                        headlineContent = {
                            Text(
                                text = option.label,
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = option.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = { RadioButton(selected = selected, onClick = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onStyleSelected(option) }
                    )
                }

                SheetSectionTitle("حجم الخط")
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetStyle.FONT_SCALES.forEach { (percent, label) ->
                        FilterChip(
                            selected = config.fontScale == percent,
                            onClick = { onFontScaleSelected(percent) },
                            label = { Text(label) }
                        )
                    }
                }

                SheetSectionTitle("شفافية الخلفية  ·  ${tempOpacity.toInt()}%")
                Slider(
                    value = tempOpacity,
                    onValueChange = { tempOpacity = it },
                    onValueChangeFinished = { onOpacityChangeFinished(tempOpacity.toInt()) },
                    valueRange = WidgetStyle.MIN_OPACITY.toFloat()..100f,
                    steps = 7,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun SheetSectionTitle(text: String) {
    Column {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
