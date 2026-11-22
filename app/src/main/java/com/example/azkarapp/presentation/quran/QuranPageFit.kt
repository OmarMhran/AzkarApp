package com.example.azkarapp.presentation.quran

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.PageBlock

internal object PageFit {

    const val LINE_HEIGHT_FACTOR = 1.85f
    const val PAGE_H_PADDING_DP = 16f
    const val PAGE_V_PADDING_DP = 8f
    const val GAP_DP = 10f

    private const val MIN_SP = 13f
    private const val STEP_SP = 0.5f
    private const val TITLE_BLOCK_DP = 52f
    private const val FOOTER_DP = 52f
    private const val SAFETY = 0.97f

    fun versesText(ayahs: List<Ayah>): String =
        ayahs.joinToString(separator = " ") { "${quranDisplayText(it.text)} ${ayahMarker(it.number)}" }

    fun style(sizeSp: Float, family: FontFamily) = TextStyle(
        fontFamily = family,
        fontSize = sizeSp.sp,
        lineHeight = (sizeSp * LINE_HEIGHT_FACTOR).sp,
        textAlign = TextAlign.Justify,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    fun fit(
        measurer: TextMeasurer,
        density: Density,
        family: FontFamily,
        page: MushafPage,
        widthPx: Int,
        heightPx: Int,
        maxSp: Float
    ): Float {
        if (widthPx <= 0 || heightPx <= 0) return maxSp
        val verses = page.blocks.filterIsInstance<PageBlock.Verses>().map { versesText(it.ayahs) }
        val titles = page.blocks.filterIsInstance<PageBlock.SurahTitle>()
        val gapPx = with(density) { GAP_DP.dp.toPx() }
        val fixedPx = with(density) { (TITLE_BLOCK_DP * titles.size + FOOTER_DP).dp.toPx() } +
                gapPx * (page.blocks.size + 1)
        val constraints = Constraints(maxWidth = widthPx)

        fun fits(sizeSp: Float): Boolean {
            val style = style(sizeSp, family)
            var total = fixedPx
            titles.forEach { title ->
                if (title.surah.bismillah.isNotBlank()) {
                    total += gapPx + measurer.measure(
                        AnnotatedString(title.surah.bismillah), style, constraints = constraints
                    ).size.height
                }
            }
            verses.forEach { text ->
                total += measurer.measure(AnnotatedString(text), style, constraints = constraints).size.height
            }
            return total <= heightPx * SAFETY
        }

        val high = maxSp.coerceAtLeast(MIN_SP)
        if (fits(high)) return high

        var lo = 0
        var hi = ((high - MIN_SP) / STEP_SP).toInt()
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (fits(MIN_SP + mid * STEP_SP)) lo = mid else hi = mid - 1
        }
        return MIN_SP + lo * STEP_SP
    }
}