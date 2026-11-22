package com.example.azkarapp.presentation.main_hub.settings_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        content = content
    )
}

internal val SettingsCardMinHeight = 64.dp

@Composable
internal fun SettingsIconBadge(icon: ImageVector, active: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val tint by animateColorAsState(
        targetValue = if (active) colors.primary else colors.onSurfaceVariant,
        animationSpec = tween(180),
        label = "settingsIconTint"
    )
    val bg by animateColorAsState(
        targetValue = if (active) colors.primary.copy(alpha = 0.16f)
        else colors.surfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(180),
        label = "settingsIconBg"
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun SettingsSelectionCard(
    title: String,
    value: String,
    contentDescription: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SettingsCardMinHeight)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBadge(icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun SettingsSwitchCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsCard(modifier = Modifier.clickable { onCheckedChange(!checked) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SettingsCardMinHeight)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SelectionBottomSheet(
    title: String,
    options: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    trailingContent: (@Composable (T) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            options.forEach { option ->
                val selected = isSelected(option)
                val trailing: (@Composable () -> Unit)? =
                    if (trailingContent != null) {
                        { trailingContent(option) }
                    } else {
                        null
                    }

                ListItem(
                    headlineContent = {
                        Text(
                            text = label(option),
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingContent = { RadioButton(selected = selected, onClick = null) },
                    trailingContent = trailing,
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onSelect(option)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) onDismiss()
                        }
                    }
                )
            }
        }
    }
}