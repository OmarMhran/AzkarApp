package com.example.azkarapp.presentation.azkar_supplications


import com.example.azkarapp.presentation.common.ErrorRetry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SupplicationsScreen(
    viewModel: SupplicationsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fontSize = state.fontSize

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.supplications.size })

    LaunchedEffect(state.initialIndex) {
        if (state.supplications.isNotEmpty() && state.initialIndex > 0 && state.initialIndex < state.supplications.size) {
            pagerState.scrollToPage(state.initialIndex)
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val activeGreenColor = if (isDark) Color(0xFFE5C158) else Color(0xFF8A6A1F)
    val tint = if (isDark) 0.16f else 0.10f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(activeGreenColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.categoryName,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = activeGreenColor
                    )
                }

                if (state.supplications.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        userScrollEnabled = true
                    ) { page ->
                        val supp = state.supplications[page]
                        val currentCount = state.currentCounts[supp.id] ?: supp.repeat
                        val isFinished = currentCount == 0

                        val screenBgColor by animateColorAsState(
                            targetValue = if (isFinished) activeGreenColor.copy(alpha = tint) else activeGreenColor.copy(alpha = 0f),
                            animationSpec = tween(durationMillis = 300),
                            label = "bgColor"
                        )

                        val progressValue = if (isFinished || supp.repeat == 0) 1f else {
                            (supp.repeat - currentCount).toFloat() / supp.repeat.toFloat()
                        }

                        val animatedProgress by animateFloatAsState(
                            targetValue = progressValue,
                            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                            label = "progressAnimation"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(screenBgColor)
                                .clickable(
                                    enabled = !isFinished,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.onAction(
                                        SupplicationsAction.Tap(supp.id) {
                                            scope.launch {
                                                delay(400)
                                                if (pagerState.currentPage < state.supplications.size - 1) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                }
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = activeGreenColor.copy(alpha = 0.18f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "الذكر ${page + 1} من ${state.supplications.size}",
                                    fontSize = 12.5.sp,
                                    color = activeGreenColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = supp.bodyVocalized,
                                    fontSize = fontSize.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFinished) MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.38f
                                    ) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    lineHeight = (fontSize * 1.6f).sp
                                )

                                if (!supp.note.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Surface(
                                        color = activeGreenColor.copy(alpha = tint),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            activeGreenColor.copy(alpha = 0.35f)
                                        )
                                    ) {
                                        Text(
                                            text = supp.note,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(115.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = activeGreenColor.copy(alpha = 0.2f),
                                        strokeWidth = 8.dp,
                                    )

                                    CircularProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = activeGreenColor,
                                        strokeWidth = 8.dp,
                                    )

                                    if (isFinished) {
                                        Text(
                                            text = "تم",
                                            color = activeGreenColor,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    } else {
                                        Text(
                                            text = "$currentCount",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (isFinished) "تم إكمال الذكر بنجاح" else "متبقي $currentCount عَدّة من أصل ${supp.repeat}",
                                    fontSize = 14.5.sp,
                                    color = if (isFinished) activeGreenColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                state.error?.takeIf { !state.isLoading }?.let { message ->
                    ErrorRetry(
                        message = message,
                        onRetry = { viewModel.onAction(SupplicationsAction.Retry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (state.showCompletionDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onAction(SupplicationsAction.DismissCompletionDialog) },
                        modifier = Modifier.width(360.dp),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = activeGreenColor.copy(alpha = tint).compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh),
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "أحسنت",
                                    color = activeGreenColor,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "أتممت ${state.categoryName} بحمد الله 🌿",
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        confirmButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    8.dp,
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                TextButton(
                                    onClick = { viewModel.onAction(SupplicationsAction.DismissCompletionDialog) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("البقاء هنا", color = activeGreenColor)
                                }

                                Button(
                                    onClick = {
                                        viewModel.onAction(SupplicationsAction.DismissCompletionDialog)
                                        onBack()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = activeGreenColor,
                                        contentColor = if (isDark) Color(0xFF2A2000) else Color.White
                                    )
                                ) {
                                    Text("رجوع")
                                }
                            }
                        },
                        dismissButton = null
                    )
                }
            }
        }
    }
}