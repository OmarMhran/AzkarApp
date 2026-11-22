package com.example.azkarapp.presentation.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.AyahKey
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.PageBlock
import com.example.azkarapp.presentation.common.ErrorRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

@Composable
fun QuranReaderScreen(
    viewModel: QuranReaderViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProvideQuranFont {
        QuranReaderContent(state = state, onAction = viewModel::onAction, onBackClick = onBackClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun QuranReaderContent(
    state: QuranReaderUiState,
    onAction: (QuranReaderAction) -> Unit,
    onBackClick: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = state.initialPage - 1,
        pageCount = { MushafPage.TOTAL_PAGES }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .debounce(600)
            .collect { onAction(QuranReaderAction.PageSettled(it + 1)) }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .filter { it }
            .collect { onAction(QuranReaderAction.ClearHighlight) }
    }

    val currentPage = state.pages[pagerState.currentPage + 1]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentPage?.let { "سورة ${it.surahName}" } ?: "المصحف الشريف",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        currentPage?.let {
                            Text(
                                text = "الجزء ${it.juz}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            beyondViewportPageCount = 1,
            key = { it }
        ) { index ->
            val pageNumber = index + 1
            LaunchedEffect(pageNumber) { onAction(QuranReaderAction.PageRequested(pageNumber)) }

            val page = state.pages[pageNumber]
            when {
                page != null -> MushafPageContent(
                    page = page,
                    bookmarked = state.bookmarked,
                    highlighted = state.highlighted,
                    onAyahTap = { ayah ->
                        onAction(
                            QuranReaderAction.AyahSelected(
                                SelectedAyah(
                                    key = AyahKey(ayah.surahNumber, ayah.number),
                                    surahName = page.surahNames[ayah.surahNumber].orEmpty(),
                                    text = ayah.text
                                )
                            )
                        )
                    }
                )

                pageNumber in state.failedPages -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    ErrorRetry(
                        message = "تعذر تحميل الصفحة $pageNumber",
                        onRetry = { onAction(QuranReaderAction.RetryPage(pageNumber)) }
                    )
                }

                else -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    state.selectedAyah?.let { selected ->
        AlertDialog(
            onDismissRequest = { onAction(QuranReaderAction.DismissAyahMenu) },
            title = { Text("سورة ${selected.surahName} • آية ${selected.key.ayahNumber}") },
            text = {
                Text(
                    text = selected.text,
                    fontFamily = LocalQuranFont.current,
                    fontSize = 18.sp,
                    lineHeight = 32.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(QuranReaderAction.ToggleSelectedBookmark) }) {
                    Text(if (state.isSelectedBookmarked) "إزالة من المفضلة" else "إضافة إلى المفضلة")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(QuranReaderAction.DismissAyahMenu) }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

private const val BOLD_OFFSET_EM = 0.025f

private val PAGE_H_PADDING = 20.dp

private const val PAGE_V_PADDING_FRACTION = 0.09f
private const val PAGE_TOP_FRACTION_WITH_TITLE = 0.015f
private val FOOTER_HEIGHT = 28.dp

@Composable
private fun MushafPageContent(
    page: MushafPage,
    bookmarked: Set<AyahKey>,
    highlighted: AyahKey?,
    onAyahTap: (Ayah) -> Unit
) {
    val spineColor = MaterialTheme.colorScheme.onSurface
    val spineOnLeft = page.number % 2 == 1
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val shadowWidth = 28.dp.toPx()
                val left = if (spineOnLeft) 0f else size.width - shadowWidth
                val colors = if (spineOnLeft) {
                    listOf(spineColor.copy(alpha = 0.30f), Color.Transparent)
                } else {
                    listOf(Color.Transparent, spineColor.copy(alpha = 0.30f))
                }
                drawRect(
                    brush = Brush.horizontalGradient(colors, startX = left, endX = left + shadowWidth),
                    topLeft = Offset(left, 0f),
                    size = androidx.compose.ui.geometry.Size(shadowWidth, size.height)
                )
                val lineWidth = 1.5.dp.toPx()
                val outerX = if (spineOnLeft) 0f else size.width - lineWidth
                val innerX = if (spineOnLeft) 6.dp.toPx() else size.width - 6.dp.toPx() - lineWidth
                drawRect(
                    color = spineColor.copy(alpha = 0.55f),
                    topLeft = Offset(outerX, 0f),
                    size = androidx.compose.ui.geometry.Size(lineWidth, size.height)
                )
                drawRect(
                    color = spineColor.copy(alpha = 0.35f),
                    topLeft = Offset(innerX, 0f),
                    size = androidx.compose.ui.geometry.Size(lineWidth, size.height)
                )
            }
    ) {
        val density = LocalDensity.current
        val family = LocalQuranFont.current
        val customFont = LocalQuranFontIsCustom.current
        val resolver = LocalFontFamilyResolver.current
        val direction = LocalLayoutDirection.current

        val topPadding = maxHeight * PAGE_V_PADDING_FRACTION
        val bottomPadding = maxHeight * PAGE_V_PADDING_FRACTION
        val widthPx = with(density) { (maxWidth - PAGE_H_PADDING * 2).toPx() }
        val heightPx = with(density) { (maxHeight - topPadding - bottomPadding - FOOTER_HEIGHT).toPx() }

        val key = LayoutKey(page.number, widthPx.toInt(), heightPx.toInt(), family, customFont)
        val layout by produceState<PageLayout?>(PageLayoutCache.get(key), key) {
            if (value != null) return@produceState
            value = withContext(Dispatchers.Default) {
                // TextMeasurer مخصوص للخيط ده (الـ cache بتاعه مش thread-safe).
                val measurer = TextMeasurer(resolver, density, direction)
                MushafLayoutEngine.compute(page, customFont, measurer, density, family, widthPx, heightPx)
            }.also { PageLayoutCache.put(key, it) }
        }

        val current = layout
        if (current == null) {
            Box(Modifier.fillMaxSize())
            return@BoxWithConstraints
        }

        val slotHeight = with(density) { current.slotHeightPx.toDp() }
        if (current.header.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topPadding)
                    .padding(start = PAGE_H_PADDING, end = PAGE_H_PADDING, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
            ) {
                current.header.forEach { slot ->
                    when (slot) {
                        is PageSlot.Title -> HeaderTitle(slot)
                        is PageSlot.Bismillah -> Text(
                            text = slot.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
                            textAlign = TextAlign.Center,
                            softWrap = false,
                            maxLines = 1,
                            fontFamily = LocalQuranFont.current,
                            fontSize = with(density) { (slot.fontSizePx * 0.85f).toSp() },
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        else -> Unit
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = PAGE_H_PADDING, end = PAGE_H_PADDING, top = topPadding, bottom = bottomPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(if (current.overflow) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = if (current.centerVertically) Arrangement.Center else Arrangement.Top
            ) {
                current.slots.forEach { slot ->
                    when (slot) {
                        is PageSlot.Title -> TitleSlot(slot, slotHeight * MushafLayoutEngine.TITLE_FACTOR)
                        is PageSlot.Bismillah -> BismillahSlot(slot, slotHeight)
                        is PageSlot.TextLine -> LineSlot(
                            line = slot,
                            slotHeight = slotHeight,
                            bookmarked = bookmarked,
                            highlighted = highlighted,
                            onAyahTap = onAyahTap
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(FOOTER_HEIGHT), Alignment.Center) {
                Text(
                    text = arabicIndic(page.number),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeaderTitle(slot: PageSlot.Title) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxWidth().height(30.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.2.dp, primary.copy(alpha = 0.55f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "✦  سورة ${slot.surah.name}  ✦",
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontFamily = LocalQuranFont.current,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }
    }
}

@Composable
private fun TitleSlot(slot: PageSlot.Title, slotHeight: androidx.compose.ui.unit.Dp) {
    val primary = MaterialTheme.colorScheme.primary
    Box(Modifier.fillMaxWidth().height(slotHeight), Alignment.TopCenter) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(slotHeight * 0.62f),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.5.dp, primary.copy(alpha = 0.55f))
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .border(0.8.dp, primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦  سورة ${slot.surah.name}  ✦",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontFamily = LocalQuranFont.current,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )
            }
        }
    }
}

@Composable
private fun BismillahSlot(slot: PageSlot.Bismillah, slotHeight: androidx.compose.ui.unit.Dp) {
    val density = LocalDensity.current
    Box(Modifier.fillMaxWidth().height(slotHeight), Alignment.Center) {
        Text(
            text = slot.text,
            modifier = Modifier.fillMaxWidth().wrapContentHeight(align = Alignment.CenterVertically, unbounded = true),
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
            fontFamily = LocalQuranFont.current,
            fontWeight = MushafLayoutEngine.MUSHAF_WEIGHT,
            fontSize = with(density) { slot.fontSizePx.toSp() },
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun LineSlot(
    line: PageSlot.TextLine,
    slotHeight: androidx.compose.ui.unit.Dp,
    bookmarked: Set<AyahKey>,
    highlighted: AyahKey?,
    onAyahTap: (Ayah) -> Unit
) {
    val density = LocalDensity.current
    val primary = MaterialTheme.colorScheme.primary
    val bookmarkColor = MaterialTheme.colorScheme.tertiary
    val searchHighlight = primary.copy(alpha = 0.85f)
    val onHighlight = MaterialTheme.colorScheme.onPrimary

    val textColor = MaterialTheme.colorScheme.onSurface
    val boldOffset = line.fontSizePx * BOLD_OFFSET_EM
    val style = MushafLayoutEngine.style(line.fontSizePx, density, LocalQuranFont.current)
        .copy(shadow = Shadow(textColor, Offset(boldOffset, 0f), 0f))

    val built = remember(line, bookmarked, highlighted, primary, bookmarkColor, searchHighlight, onHighlight) {
        val ranges = ArrayList<Pair<IntRange, Ayah>>(line.tokens.size)
        val text = buildAnnotatedString {
            line.tokens.forEachIndexed { index, token ->
                val key = AyahKey(token.ayah.surahNumber, token.ayah.number)
                val bg = if (highlighted == key) searchHighlight else Color.Unspecified
                val start = length
                val isHit = highlighted == key
                withStyle(
                    if (isHit) SpanStyle(
                        background = bg,
                        color = onHighlight,
                        shadow = Shadow(onHighlight, Offset(boldOffset, 0f), 0f)
                    ) else SpanStyle(background = bg)
                ) {
                    if (token.markerStart >= 0) {
                        append(token.text.substring(0, token.markerStart))
                        val markerColor = when {
                            key in bookmarked -> bookmarkColor
                            highlighted == key -> onHighlight
                            else -> primary
                        }
                        withStyle(SpanStyle(color = markerColor, shadow = Shadow(markerColor, Offset(boldOffset, 0f), 0f))) {
                            append(token.text.substring(token.markerStart))
                        }
                    } else {
                        append(token.text)
                    }
                    val following = line.tokens.getOrNull(index + 1)
                    if (following != null && following.ayah === token.ayah) append(" ")
                }
                ranges.add((start until length) to token.ayah)
                val following = line.tokens.getOrNull(index + 1)
                if (following != null && following.ayah !== token.ayah) append(" ")
            }
        }
        text to ranges
    }
    val (annotated, ranges) = built
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(Modifier.fillMaxWidth().height(slotHeight), Alignment.Center) {
        Text(
            text = annotated,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true)
                .pointerInput(ranges) {
                    detectTapGestures { position ->
                        val offset = layoutResult?.getOffsetForPosition(position) ?: return@detectTapGestures
                        val hit = ranges.firstOrNull { offset in it.first }
                            ?: ranges.lastOrNull { it.first.first <= offset }
                        hit?.let { onAyahTap(it.second) }
                    }
                },
            onTextLayout = { layoutResult = it },
            softWrap = false,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = style,
            color = textColor
        )
    }
}