package com.example.azkarapp.presentation.main_hub.settings_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
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
    var isThemeSheetVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        SettingsHeader()

        FontSizeCard(
            savedSize = state.fontSize,
            onSizeChangeFinished = { onAction(SettingsAction.FontSizeChanged(it)) }
        )

        SettingsSelectionCard(
            title = "مظهر التطبيق",
            value = state.themeMode.label,
            contentDescription = "اختر المظهر",
            onClick = { isThemeSheetVisible = true }
        )

        SettingsSwitchCard(
            title = "اهتزاز المسبحة",
            description = "تفعيل اهتزاز خفيف عند الضغط وإتمام التسبيح",
            checked = state.isVibrationEnabled,
            onCheckedChange = { onAction(SettingsAction.VibrationToggled(it)) }
        )

        WidgetSettingsCards(
            config = state.widget,
            onStyleSelected = { onAction(SettingsAction.WidgetStyleSelected(it)) },
            onFontScaleSelected = { onAction(SettingsAction.WidgetFontScaleSelected(it)) },
            onOpacityChangeFinished = { onAction(SettingsAction.WidgetOpacityChanged(it)) }
        )

        SettingsSelectionCard(
            title = "صوت الأذان والتنبيهات",
            value = state.adhanName,
            contentDescription = "اختر الصوت",
            onClick = { isAdhanSheetVisible = true }
        )

        SettingsSwitchCard(
            title = "إشعار الذكر المستمر",
            description = "عرض ذكر عشوائي في شريط الإشعارات (يختفي عند الضغط)",
            checked = state.isZikrNotificationEnabled,
            onCheckedChange = { onAction(SettingsAction.ZikrNotificationToggled(it)) }
        )

        if (state.isZikrNotificationEnabled) {
            SettingsSelectionCard(
                title = "تغيير الذكر تلقائياً في الإشعار",
                value = state.zikrIntervalName,
                contentDescription = "اختر الوقت",
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

    if (isThemeSheetVisible) {
        SelectionBottomSheet(
            title = "اختر مظهر التطبيق",
            options = ThemeMode.entries,
            isSelected = { it == state.themeMode },
            label = { it.label },
            onSelect = { onAction(SettingsAction.ThemeSelected(it)) },
            onDismiss = { isThemeSheetVisible = false }
        )
    }
}

@Composable
private fun SettingsHeader() {
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
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "الإعدادات",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun AdhanPreviewButton(isPlaying: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "تشغيل معاينة",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
