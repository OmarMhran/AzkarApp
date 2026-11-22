package com.example.azkarapp.presentation.main_hub.settings_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azkarapp.domain.model.ThemeMode


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.onAction(SettingsAction.AdhanPreviewStopped) }
    }

    SettingsContent(state = state, onAction = viewModel::onAction)

}


@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit
) {
    var isAdhanSheetVisible by remember { mutableStateOf(false) }
    var isIntervalSheetVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 36.dp
                )
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.Start
    ) {
        SettingsHeader()

        FontSizeCard(
            savedSize = state.fontSize,
            onSizeChangeFinished = { onAction(SettingsAction.FontSizeChanged(it)) }
        )

        ThemeModeCard(
            selectedMode = state.themeMode,
            onModeSelected = { onAction(SettingsAction.ThemeSelected(it)) }
        )

        CompactSwitchCard(
            title = "اهتزاز المسبحة",
            icon = Icons.Rounded.Vibration,
            checked = state.isVibrationEnabled,
            onCheckedChange = { onAction(SettingsAction.VibrationToggled(it)) }
        )

        WidgetSettingsCards(
            config = state.widget,
            onStyleSelected = { onAction(SettingsAction.WidgetStyleSelected(it)) },
            onFontScaleSelected = { onAction(SettingsAction.WidgetFontScaleSelected(it)) },
            onOpacityChangeFinished = { onAction(SettingsAction.WidgetOpacityChanged(it)) }
        )

        CompactActionCard(
            title = "صوت الأذان والتنبيهات",
            value = state.adhanName,
            icon = Icons.Rounded.VolumeUp,
            onClick = { isAdhanSheetVisible = true }
        )

        CompactSwitchCard(
            title = "إشعار الذكر المستمر",
            icon = Icons.Rounded.NotificationsActive,
            checked = state.isZikrNotificationEnabled,
            onCheckedChange = { onAction(SettingsAction.ZikrNotificationToggled(it)) }
        )

        if (state.isZikrNotificationEnabled) {
            CompactActionCard(
                title = "تغيير الذكر تلقائياً",
                value = state.zikrIntervalName,
                icon = Icons.Rounded.Schedule,
                onClick = { isIntervalSheetVisible = true }
            )
        }
    }

    if (isAdhanSheetVisible) {
        SelectionBottomSheet(
            title = "اختر صوت الأذان",
            options = SettingsOptions.adhanSounds,
            isSelected = { it.id == state.adhanSoundId },
            label = { it.name },
            onSelect = { onAction(SettingsAction.AdhanSelected(it.id)) },
            onDismiss = {
                onAction(SettingsAction.AdhanPreviewStopped)
                isAdhanSheetVisible = false
            },
            trailingContent = { sound ->
                AdhanPreviewButton(
                    isPlaying = state.previewingSoundId == sound.id,
                    onClick = { onAction(SettingsAction.AdhanPreviewToggled(sound.id)) }
                )
            }
        )
    }

    if (isIntervalSheetVisible) {
        SelectionBottomSheet(
            title = "اختر الفاصل الزمني",
            options = SettingsOptions.zikrIntervals,
            isSelected = { it.minutes == state.zikrIntervalMinutes },
            label = { it.name },
            onSelect = { onAction(SettingsAction.ZikrIntervalSelected(it.minutes)) },
            onDismiss = { isIntervalSheetVisible = false }
        )
    }
}

@Composable
private fun CompactSwitchCard(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    SettingsCard(modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable { onCheckedChange(!checked) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SettingsCardMinHeight)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsIconBadge(icon = icon, active = checked)
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onPrimary,
                    checkedTrackColor = colors.primary,
                    uncheckedThumbColor = colors.onSurfaceVariant,
                    uncheckedTrackColor = colors.surfaceVariant,
                    uncheckedBorderColor = colors.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun CompactActionCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    SettingsCard(modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SettingsCardMinHeight)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsIconBadge(icon = icon)
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeModeCard(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val activePillBg = colors.primary.copy(alpha = 0.18f)

    val orderedModes = remember {
        ThemeMode.entries.sortedBy { mode ->
            when (mode.name.uppercase()) {
                "LIGHT" -> 0
                "DARK" -> 1
                else -> 2
            }
        }
    }

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
                SettingsIconBadge(icon = Icons.Rounded.Palette, active = true)
                Text(
                    text = "مظهر التطبيق",
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
                orderedModes.forEach { mode ->
                    val isSelected = mode == selectedMode
                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) activePillBg else Color.Transparent,
                        animationSpec = tween(160),
                        label = "themeModePillBg"
                    )
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) colors.primary
                        else colors.onSurfaceVariant.copy(alpha = 0.75f),
                        animationSpec = tween(160),
                        label = "themeModeIconTint"
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(50))
                            .background(pillBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onModeSelected(mode) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = themeModeIcon(mode),
                            contentDescription = mode.label,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun themeModeIcon(mode: ThemeMode): ImageVector {
    return when (mode.name.uppercase()) {
        "LIGHT" -> Icons.Rounded.LightMode
        "DARK" -> Icons.Rounded.DarkMode
        else -> Icons.Rounded.BrightnessAuto
    }
}

@Composable
private fun SettingsHeader() {
    val colors = MaterialTheme.colorScheme
    val deepGreen = colors.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(deepGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "الإعدادات",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = colors.onBackground
        )
    }
}

@Composable
private fun AdhanPreviewButton(isPlaying: Boolean, onClick: () -> Unit) {
    val deepGreen = MaterialTheme.colorScheme.primary

    IconButton(onClick = onClick) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(deepGreen)
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "تشغيل معاينة",
                tint = deepGreen
            )
        }
    }
}