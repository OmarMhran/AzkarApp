package com.example.azkarapp.presentation.hafz

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azkarapp.domain.model.HafzProgress
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.recitation.RecitationResult
import com.example.azkarapp.domain.recitation.WordStatus
import com.example.azkarapp.presentation.common.ErrorRetry
import com.example.azkarapp.presentation.quran.LocalQuranFont
import com.example.azkarapp.presentation.quran.LocalQuranFontIsCustom
import com.example.azkarapp.presentation.quran.ProvideQuranFont
import com.example.azkarapp.presentation.quran.arabicIndic
import com.example.azkarapp.presentation.quran.ayahMarker
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun HafzScreen(
    onBackClick: () -> Unit,
    viewModel: HafzViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val accent = hafzAccent()

    ProvideQuranFont {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (state.isReady) {
                // الـ Pager بيتعمل بعد ما نعرف الصفحة الابتدائية (آخر صفحة أو اللي جاية من الرابط)
                HafzReady(state = state, viewModel = viewModel, onBackClick = onBackClick)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.error != null) {
                        ErrorRetry(
                            message = state.error.orEmpty(),
                            onRetry = { viewModel.onAction(HafzAction.Retry) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = accent
                        )
                    }
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HafzReady(
    state: HafzUiState,
    viewModel: HafzViewModel,
    onBackClick: () -> Unit
) {
    val accent = hafzAccent()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onAction(if (granted) HafzAction.Start else HafzAction.PermissionDenied)
    }

    fun startWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.onAction(HafzAction.Start)
        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val pagerState = rememberPagerState(
        initialPage = (state.initialPage - 1).coerceIn(0, MushafPage.TOTAL_PAGES - 1),
        pageCount = { MushafPage.TOTAL_PAGES }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { viewModel.onAction(HafzAction.PageChanged(it + 1)) }
    }
    LaunchedEffect(pagerState) {
        viewModel.jumps.collect { page ->
            val index = page - 1
            if (abs(index - pagerState.currentPage) <= 2) pagerState.animateScrollToPage(index)
            else pagerState.scrollToPage(index)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حفظ القرآن",
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
                    actions = {
                        IconButton(onClick = { viewModel.onAction(HafzAction.OpenSearch) }) {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = accent)
                        }
                        IconButton(onClick = { viewModel.onAction(HafzAction.ToggleShowText) }) {
                            Icon(
                                imageVector = if (state.showText) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (state.showText) "إخفاء النص" else "إظهار النص",
                                tint = accent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                ProgressStrip(state.progress)
            }
        },
        bottomBar = {
            ControlsBar(
                page = state.currentPage,
                isListening = state.isListening,
                isMemorized = state.isMemorized,
                result = state.result,
                message = state.message,
                onMic = {
                    if (state.isListening) viewModel.onAction(HafzAction.Stop)
                    else startWithPermission()
                },
                onReset = { viewModel.onAction(HafzAction.Reset) },
                onToggleMemorized = {
                    viewModel.onAction(HafzAction.SetMemorized(!state.isMemorized))
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                HorizontalPager(
                    state = pagerState,
                    key = { it },
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val page = index + 1
                    LaunchedEffect(page) { viewModel.onAction(HafzAction.EnsurePage(page)) }
                    PageView(
                        page = page,
                        state = state,
                        onRetry = { viewModel.onAction(HafzAction.EnsurePage(page)) }
                    )
                }
            }
        }
    }

    if (state.searchOpen) {
        SearchDialog(
            query = state.searchQuery,
            hits = state.searchHits,
            onQueryChange = { viewModel.onAction(HafzAction.SearchChanged(it)) },
            onPick = { viewModel.onAction(HafzAction.JumpTo(it)) },
            onDismiss = { viewModel.onAction(HafzAction.CloseSearch) }
        )
    }

    if (state.showPassedDialog) {
        PassedDialog(
            wrong = state.result.wrong,
            hasNext = state.currentPage < MushafPage.TOTAL_PAGES,
            onDismiss = { viewModel.onAction(HafzAction.DismissPassed) },
            onNext = {
                viewModel.onAction(HafzAction.DismissPassed)
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
    }
}

@Composable
private fun ProgressStrip(progress: HafzProgress) {
    val accent = hafzAccent()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${arabicIndic(progress.memorizedCount)} / ${arabicIndic(MushafPage.TOTAL_PAGES)} صفحة",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { progress.fraction },
            modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.2f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PageView(page: Int, state: HafzUiState, onRetry: () -> Unit) {
    val accent = hafzAccent()
    val content = state.pages[page]
    val isCurrent = page == state.currentPage

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            content != null -> {
                val family = LocalQuranFont.current
                val customFont = LocalQuranFontIsCustom.current
                val correctColor = MaterialTheme.colorScheme.primary
                val wrongColor = MaterialTheme.colorScheme.error
                val hiddenColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                val shownColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                val statuses = if (isCurrent) state.result.statuses else emptyList()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سورة ${content.surahName}  •  صفحة ${arabicIndic(page)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        if (page in state.progress.memorizedPages) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "محفوظة",
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        content.words.forEachIndexed { index, word ->
                            val status = statuses.getOrElse(index) { WordStatus.PENDING }
                            val reveal = state.showText || status != WordStatus.PENDING
                            val color = when (status) {
                                WordStatus.CORRECT -> correctColor
                                WordStatus.WRONG -> wrongColor
                                WordStatus.PENDING -> if (state.showText) shownColor else hiddenColor
                            }
                            Text(
                                text = if (reveal) word.display
                                else "•".repeat((word.display.length / 3).coerceIn(2, 6)),
                                fontFamily = family,
                                fontSize = if (reveal) 26.sp else 18.sp,
                                fontWeight = if (status == WordStatus.PENDING) FontWeight.Normal
                                else FontWeight.Bold,
                                color = color
                            )
                            content.ayahEnds[index]?.let { number ->
                                Text(
                                    text = ayahMarker(number, customFont),
                                    fontFamily = family,
                                    fontSize = 22.sp,
                                    color = accent.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            page in state.failedPages -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("تعذر تحميل الصفحة", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("إعادة المحاولة", color = accent) }
            }

            else -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = accent
            )
        }
    }
}

@Composable
private fun ControlsBar(
    page: Int,
    isListening: Boolean,
    isMemorized: Boolean,
    result: RecitationResult,
    message: String?,
    onMic: () -> Unit,
    onReset: () -> Unit,
    onToggleMemorized: () -> Unit
) {
    val accent = hafzAccent()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = hafzTintAlpha())
                .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh)
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val statusText = message ?: when {
                isListening -> "جاري الاستماع... اقرأ بوضوح"
                result.consumed > 0 -> "وقفت. دوس الميكروفون علشان تكمّل"
                else -> "دوس الميكروفون وابدأ من أول صفحة ${arabicIndic(page)}"
            }
            Text(
                text = statusText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = if (message != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (result.total > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "صح ${arabicIndic(result.correct)}  •  غلط ${arabicIndic(result.wrong)}  •  الباقي ${arabicIndic(result.total - result.consumed)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleMemorized) {
                    Text(
                        text = if (isMemorized) "إلغاء الحفظ" else "علّمها محفوظة",
                        color = accent,
                        fontSize = 12.sp
                    )
                }
                FilledIconButton(
                    onClick = onMic,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else accent,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "إيقاف" else "ابدأ التسميع",
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = "من الأول", tint = accent)
                }
            }
        }
    }
}

@Composable
private fun SearchDialog(
    query: String,
    hits: List<SearchHit>,
    onQueryChange: (String) -> Unit,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = hafzAccent()
    val family = LocalQuranFont.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = accent.copy(alpha = hafzTintAlpha())
                    .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh)
            ),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("اسم سورة، رقم صفحة، أو جزء من آية", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accent) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = accent.copy(alpha = 0.35f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    items(hits) { hit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(hit.page) }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = hit.title,
                                fontFamily = if (hit.isAyah) family else null,
                                fontSize = if (hit.isAyah) 18.sp else 15.sp,
                                fontWeight = if (hit.isAyah) FontWeight.Normal else FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (hit.subtitle.isNotEmpty()) {
                                Text(
                                    text = hit.subtitle,
                                    fontSize = 12.sp,
                                    color = accent
                                )
                            }
                        }
                        HorizontalDivider(color = accent.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PassedDialog(wrong: Int, hasNext: Boolean, onDismiss: () -> Unit, onNext: () -> Unit) {
    val accent = hafzAccent()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = accent.copy(alpha = hafzTintAlpha())
            .compositeOver(MaterialTheme.colorScheme.surfaceContainerHigh),
        title = {
            Text(
                text = "تسميع ناجح",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                color = accent
            )
        },
        text = {
            Text(
                text = if (wrong == 0) "اتعلّمت الصفحة محفوظة. بارك الله فيك."
                else "اتعلّمت الصفحة محفوظة (فيه ${arabicIndic(wrong)} كلمة محتاجة مراجعة).",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            if (hasNext) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) { Text("الصفحة اللي بعدها") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("البقاء هنا", color = accent) }
        }
    )
}