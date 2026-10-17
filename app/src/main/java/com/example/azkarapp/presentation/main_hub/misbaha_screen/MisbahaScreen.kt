package com.example.azkarapp.presentation.main_hub.misbaha_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private val MisbahaTabs = listOf("المسبحة", "إضافة ذكر", "السجل")

@Composable
fun MisbahaScreen(viewModel: MisbahaViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MisbahaContent(state = state, onAction = viewModel::onAction)
}

@Composable
private fun MisbahaContent(
    state: MisbahaUiState,
    onAction: (MisbahaAction) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        MisbahaHeader()

        MisbahaTabRow(
            tabs = MisbahaTabs,
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> SebhaCounterTab(
                selectedZikr = state.selectedZikr,
                azkarOptions = state.azkarOptions,
                currentCount = state.currentCount,
                onZikrSelected = { onAction(MisbahaAction.SelectZikr(it)) },
                onCounterIncrement = { onAction(MisbahaAction.Increment) },
                onReset = { onAction(MisbahaAction.Reset) }
            )

            1 -> AddCustomZikrTab(
                customAzkar = state.customAzkar,
                onZikrAdded = { name ->
                    onAction(MisbahaAction.AddCustomZikr(name) { selectedTab = 0 })
                },
                onUpdateZikr = { id, newName ->
                    onAction(MisbahaAction.UpdateCustomZikr(id, newName))
                },
                onDeleteZikr = { id -> onAction(MisbahaAction.DeleteCustomZikr(id)) }
            )

            2 -> HistoryTab(
                dailyStats = state.dailyStats,
                totalAllTime = state.totalCountAllTime,
                onClearHistory = { onAction(MisbahaAction.ClearHistory) }
            )
        }
    }
}

@Composable
private fun MisbahaHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
            text = "المسبحة الإلكترونية",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MisbahaTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                label = "chipBg"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "chipText"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelected(index) },
                shape = RoundedCornerShape(50),
                color = containerColor,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                )
            ) {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 13.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
