package com.example.azkarapp.presentation.main_hub.home_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azkarapp.presentation.hafz.HafzHomeCard
import com.example.azkarapp.ui.navigation.Screen
import com.example.azkarapp.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootNavController: NavHostController,
    onNavigateToTab: (Int) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedDialogTitle by remember { mutableStateOf<String?>(null) }
    var selectedDialogContent by remember { mutableStateOf<String?>(null) }

    if (selectedDialogContent != null && selectedDialogTitle != null) {
        AlertDialog(
            onDismissRequest = {
                selectedDialogContent = null
                selectedDialogTitle = null
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            ShareUtils.shareText(context, selectedDialogContent!!)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مشاركة",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(
                        onClick = {
                            selectedDialogContent = null
                            selectedDialogTitle = null
                        }
                    ) {
                        Text(
                            text = "إغلاق",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedDialogTitle!!,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Text(
                        text = selectedDialogContent!!,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        )
    }

    // ألوان الكروت: كل كرت بلون مميز (ذهبي، تركواز، أخضر، طوبي) مع نسخة أغمق للايت وأفتح للدارك
    // علشان النص يفضل مقروء. المصحف فوق بيفضل بأخضر التطبيق.
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val gold = if (isDark) Color(0xFFE5C158) else Color(0xFF8A6A1F)
    val teal = if (isDark) Color(0xFF6BC5D2) else Color(0xFF1F7A8C)
    val green = MaterialTheme.colorScheme.primary
    val terracotta = if (isDark) Color(0xFFE8A27C) else Color(0xFF9C5430)
    val tint = if (isDark) 0.16f else 0.10f

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.onAction(HomeAction.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الرئيسية",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                state.azkarProgress?.let { progress ->
                    AzkarProgressBadge(
                        progress = progress,
                        onBadgeClick = {
                            rootNavController.navigate(
                                "supplications_screen/${progress.categoryId}/${progress.categoryName}"
                            )
                        }
                    )
                }
            }

            // 1. المصحف الشريف
            Card(
                onClick = { rootNavController.navigate(Screen.Quran.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "المصحف الشريف",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "اقرأ القرآن الكريم وابحث في آياته بدون إنترنت",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    state.quranLastRead?.let { last ->
                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            onClick = {
                                rootNavController.navigate(
                                    Screen.QuranReader.createRoute(last.page)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "أكمل القراءة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "سورة ${last.surahName} • صفحة ${last.page}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

            }

            HafzHomeCard(onClick = { rootNavController.navigate(Screen.Hafz.route) })


            // 2. Quick Grid Section: الأذكار وأسماء الله الحسنى
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Azkar Card
                Card(
                    onClick = { rootNavController.navigate(Screen.AzkarSections.route) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = gold.copy(alpha = tint)
                    ),
                    border = BorderStroke(
                        1.dp,
                        gold.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "الأذكار والأدعية",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = gold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المكتبة الكاملة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Asmaa Allah Al-Husna Card
                Card(
                    onClick = { rootNavController.navigate("names_screen") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = teal.copy(alpha = tint)
                    ),
                    border = BorderStroke(
                        1.dp,
                        teal.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "أسماء الله الحسنى",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = teal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الأسماء ومعانيها",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Daily Content: آية اليوم
            Card(
                onClick = {
                    selectedDialogTitle = "آية اليوم"
                    selectedDialogContent = state.dailyAyah
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = green.copy(alpha = tint)
                ),
                border = BorderStroke(
                    1.dp,
                    green.copy(alpha = 0.30f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // شريط ملوّن على الحافة (يمين في RTL)
                            val w = 4.dp.toPx()
                            drawRect(green, topLeft = Offset(size.width - w, 0f), size = Size(w, size.height))
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آية اليوم",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = green
                        )
                        IconButton(
                            onClick = { ShareUtils.shareText(context, state.dailyAyah) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                tint = green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = state.dailyAyah,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        lineHeight = 22.sp,
                        maxLines = 3
                    )
                }
            }

            // 4. Daily Content: حديث اليوم
            Card(
                onClick = {
                    selectedDialogTitle = "حديث اليوم"
                    selectedDialogContent = state.dailyHadith
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = terracotta.copy(alpha = tint)
                ),
                border = BorderStroke(
                    1.dp,
                    terracotta.copy(alpha = 0.30f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // شريط ملوّن على الحافة (يمين في RTL)
                            val w = 4.dp.toPx()
                            drawRect(terracotta, topLeft = Offset(size.width - w, 0f), size = Size(w, size.height))
                        }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حديث اليوم",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = terracotta
                        )
                        IconButton(
                            onClick = { ShareUtils.shareText(context, state.dailyHadith) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                tint = terracotta,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = state.dailyHadith,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        lineHeight = 22.sp,
                        maxLines = 3
                    )
                }
            }
        }
    }
}