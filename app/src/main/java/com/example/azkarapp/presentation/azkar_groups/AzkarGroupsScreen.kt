@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.azkarapp.presentation.azkar_groups

import com.example.azkarapp.presentation.common.ErrorRetry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun azkarGold(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) Color(0xFFE5C158) else Color(0xFF8A6A1F)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarGroupsScreen(
    viewModel: AzkarGroupsViewModel,
    onGroupClick: (groupId: Int, groupName: String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gold = azkarGold()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(gold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الأذكار والأدعية",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = gold
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.groups.size) { index ->
                        val group = state.groups[index]
                        MinimalGroupCard(
                            index = index + 1,
                            groupName = group.name,
                            onClick = { onGroupClick(group.id, group.name) }
                        )
                    }
                }

                state.error?.takeIf { !state.isLoading }?.let { message ->
                    ErrorRetry(
                        message = message,
                        onRetry = { viewModel.onAction(AzkarGroupsAction.Retry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun MinimalGroupCard(
    index: Int,
    groupName: String,
    onClick: () -> Unit
) {
    val gold = azkarGold()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "cardScaleAnimation"
    )

    val formattedIndex = remember(index) {
        if (index < 10) "0$index" else "$index"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .scale(cardScale),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = gold.copy(alpha = if (isDark) 0.16f else 0.10f).compositeOver(MaterialTheme.colorScheme.surface)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = gold.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.5.dp)
                    .fillMaxHeight(0.45f)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(gold)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(gold.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedIndex,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = gold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = groupName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}