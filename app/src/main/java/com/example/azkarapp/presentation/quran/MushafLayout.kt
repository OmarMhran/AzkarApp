package com.example.azkarapp.presentation.quran

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.model.MushafPage
import com.example.azkarapp.domain.model.PageBlock
import com.example.azkarapp.domain.model.Surah
import java.util.concurrent.ConcurrentHashMap

internal class LineToken(
    val text: String,
    val ayah: Ayah,
    val markerStart: Int
)

internal sealed interface PageSlot {
    class Title(val surah: Surah) : PageSlot
    class Bismillah(val text: String, val fontSizePx: Float) : PageSlot
    class TextLine(
        val tokens: List<LineToken>,
        val fontSizePx: Float,
        /** سطر أخير في آخر السورة: ما بيتمددش وبيتوسّط. */
        val centered: Boolean
    ) : PageSlot
}

internal data class LayoutKey(
    val page: Int,
    val widthPx: Int,
    val heightPx: Int,
    val family: FontFamily,
    val customFont: Boolean
)

/** كاش للصفحات اللي اتحسبت (آخر 16 صفحة) علشان التنقل ما يعيدش الحساب. */
internal object PageLayoutCache {
    private val map = object : LinkedHashMap<LayoutKey, PageLayout>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<LayoutKey, PageLayout>?) = size > 16
    }

    @Synchronized
    fun get(key: LayoutKey): PageLayout? = map[key]

    @Synchronized
    fun put(key: LayoutKey, layout: PageLayout) {
        map[key] = layout
    }
}

internal class PageLayout(
    val slots: List<PageSlot>,
    val slotHeightPx: Float,
    /** أول صفحتين: السطور القليلة بتتوسّط رأسياً بنفس تباعد الصفحات العادية. */
    val centerVertically: Boolean,
    /** عنوان وبسملة سورة بتبدأ من أول الصفحة: بيترسموا في الهامش اللي فوق، فالآيات تبدأ من أول سطر. */
    val header: List<PageSlot>,
    /** true لو الصفحة ما اتحطتش في الشاشة (شاشة قصيرة جداً): بتتسكرول. */
    val overflow: Boolean
)

/**
 * بيرتّب صفحة المصحف زي المطبوع: 15 سطر، كل سطر متملي من الحافة للحافة.
 * 1) بيقيس عرض كل كلمة بالخط الفعلي.
 * 2) بيختار أكبر حجم خط تتوزع بيه الكلمات على عدد الأسطر المطلوب.
 * 3) بيوزّع الكلمات على الأسطر بحيث الأسطر تبقى متقاربة في الامتلاء.
 * 4) بيظبط حجم خط كل سطر لحد ما يملا العرض بالظبط.
 * لازم يتنادى من خيط خلفي ومعاه TextMeasurer مخصوص (مش مشترك).
 */
internal object MushafLayoutEngine {

    /** عرض الكلمات (بحجم REF_PX) مشترك بين كل الصفحات علشان ما نقيسش نفس الكلمة مرتين. */
    private val widthCache = ConcurrentHashMap<Pair<FontFamily, String>, Float>()

    private const val REF_PX = 100f

    /** وزن خط المصحف. لو الخط لسه رفيع غيّره لـ FontWeight.ExtraBold، ولو تقيل غيّره لـ SemiBold. */
    val MUSHAF_WEIGHT = FontWeight.Normal

    /** سطر عنوان السورة بياخد ارتفاع أكبر من سطر الآيات علشان المسافة بينه وبين الآيات تبان. */
    const val TITLE_FACTOR = 1.5f
    /** أقصى عدد أسطر بنزوّدها لصفحة قصيرة. أكتر من كده معناه إن الخط صغير والأفضل نسيبها. */
    private const val MAX_EXTRA_LINES = 5
    private const val FIXED_SIZE_DIVISOR = 19f
    private const val LINE_FACTOR = 1.9f
    private const val MAX_STRETCH = 1.1f
    private const val MIN_STRETCH = 0.9f
    private const val SAFETY = 0.997f
    private val INF = Float.MAX_VALUE / 4

    fun style(sizePx: Float, density: Density, family: FontFamily) = TextStyle(
        fontFamily = family,
        fontSize = with(density) { sizePx.toSp() },
        fontWeight = MUSHAF_WEIGHT,
        lineHeight = with(density) { (sizePx * 1.5f).toSp() },
        textDirection = TextDirection.Rtl,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    )

    private class Block(
        val tokens: List<LineToken>,
        val widths: FloatArray,
        val endsSurah: Boolean
    )

    fun compute(
        page: MushafPage,
        customFont: Boolean,
        measurer: TextMeasurer,
        density: Density,
        family: FontFamily,
        widthPx: Float,
        heightPx: Float
    ): PageLayout {
        // سورة بتبدأ من أول الصفحة: العنوان والبسملة في الهامش فوق ومش بياخدوا أسطر.
        // لو السورة بتبدأ في نص الصفحة (السور القصيرة) بيفضلوا بين الأسطر زي ما هم.
        val headerInMargin = page.number > 2 && page.blocks.firstOrNull() is PageBlock.SurahTitle
        val bodyBlocks = if (headerInMargin) page.blocks.drop(1) else page.blocks
        val titleCount = bodyBlocks.count { it is PageBlock.SurahTitle }
        val bismillahCount = bodyBlocks.count { it is PageBlock.SurahTitle && it.surah.bismillah.isNotBlank() }
        val textTarget = when (page.number) {
            1 -> 7
            2 -> 8
            else -> (15 - titleCount - bismillahCount).coerceAtLeast(1)
        }
        val refStyle = style(REF_PX, density, family)
        fun measureRef(text: String): Float = widthCache.getOrPut(family to text) {
            measurer.measure(text, style = refStyle, softWrap = false, maxLines = 1).size.width.toFloat()
        }

        val spaceRef = (measureRef("ل ل") - 2 * measureRef("ل")).coerceAtLeast(0f)
        // عرض الكشيدة وهي متصلة بين حرفين.
        val tatweelRef = (measureRef("بـب") - 2 * measureRef("ب")).let {
            if (it > 1f) it else measureRef("ـ")
        }

        val blocks = ArrayList<Block>()
        val groupCounts = ArrayList<Int>()
        val ayahPerLine = false
        page.blocks.forEachIndexed { index, block ->
            if (block !is PageBlock.Verses) return@forEachIndexed
            val next = page.blocks.getOrNull(index + 1)
            val endsSurah = next is PageBlock.SurahTitle || (next == null && page.endsWithSurahEnd)
            val groups = ArrayList<ArrayList<LineToken>>()
            var group = ArrayList<LineToken>()
            block.ayahs.forEach { ayah ->
                val words = quranDisplayText(ayah.text).split(' ').filter { it.isNotBlank() }
                val marker = ayahMarker(ayah.number, customFont)
                words.forEachIndexed { i, word ->
                    group.add(
                        if (i == words.lastIndex) LineToken("$word $marker", ayah, word.length + 1)
                        else LineToken(word, ayah, -1)
                    )
                }
                if (ayahPerLine) {
                    groups.add(group)
                    group = ArrayList()
                }
            }
            if (!ayahPerLine) groups.add(group)
            groupCounts.add(groups.size)
            groups.forEach { tokens ->
                blocks.add(
                    Block(
                        tokens,
                        FloatArray(tokens.size) {
                            val tk = tokens[it]
                            if (tk.markerStart >= 0) {
                                measureRef(tk.text.substring(0, tk.markerStart - 1)) + spaceRef +
                                        measureRef(tk.text.substring(tk.markerStart))
                            } else {
                                measureRef(tk.text)
                            }
                        },
                        endsSurah = endsSurah || ayahPerLine
                    )
                )
            }
        }

        // ---- أكبر حجم خط تتوزع بيه كل الكتل على عدد الأسطر المطلوب ----
        fun greedyLines(block: Block, sizePx: Float): Float {
            val k = sizePx / REF_PX
            val space = spaceRef * k
            var lines = 1
            var current = 0f
            var count = 0
            for (w0 in block.widths) {
                val w = w0 * k
                if (w > widthPx) return INF
                if (count == 0) {
                    current = w
                    count = 1
                } else if (current + space + w <= widthPx) {
                    current += space + w
                    count++
                } else {
                    lines++
                    current = w
                    count = 1
                }
            }
            return lines.toFloat()
        }

        // ---- حجم خط ثابت لكل الصفحات والأسطر (بيتحدد من الشاشة بس، مش من محتوى الصفحة) ----
        val widestWord = blocks.maxOfOrNull { block -> block.widths.maxOrNull() ?: 0f } ?: 0f
        var baseSize = minOf(widthPx / FIXED_SIZE_DIVISOR, heightPx / 15f / LINE_FACTOR)
        if (widestWord > 0f) baseSize = minOf(baseSize, widthPx * 0.98f / widestWord * REF_PX)

        // عدد الأسطر لكل كتلة. لو الصفحة أقصر من 15 سطر (وما بتخلصش بنهاية سورة)،
        // بنزوّد أسطر بدل ما نسيب فراغ تحت: كل سطر بياخد كلمات أقل والباقي كشيدة.
        val lineCounts = blocks.map { greedyLines(it, baseSize).toInt().coerceAtLeast(1) }.toMutableList()
        val missing = textTarget - lineCounts.sum()
        if (page.number > 2 && !page.endsWithSurahEnd && missing in 1..MAX_EXTRA_LINES) {
            repeat(missing) {
                val candidate = blocks.indices
                    .filter { lineCounts[it] < blocks[it].tokens.size }
                    .maxByOrNull { blocks[it].tokens.size.toFloat() / lineCounts[it] }
                if (candidate != null) lineCounts[candidate]++
            }
        }
        val totalLines = lineCounts.sum()
        val slotCount = if (page.number <= 2) {
            15f
        } else {
            maxOf(textTarget, totalLines) + bismillahCount + titleCount * TITLE_FACTOR
        }
        var slotHeight = heightPx / slotCount
        var overflow = false
        if (slotHeight < baseSize * 1.3f) {
            overflow = true
            slotHeight = baseSize * 1.5f
        }

        // ---- توزيع متوازن، وكل سطر بنفس الحجم؛ الفرق بيتملي بكشيدة ----
        val textLines = ArrayList<List<PageSlot.TextLine>>()
        var blockCursor = 0
        blocks.forEach { block ->
            val lineCount = lineCounts[blockCursor++]
            val ranges = balance(block, baseSize, lineCount, spaceRef, widthPx)
            val lines = ranges.mapIndexed { lineIndex, range ->
                val tokens = block.tokens.subList(range.first, range.last + 1)
                val isLast = lineIndex == ranges.lastIndex
                if (isLast && block.endsSurah) {
                    PageSlot.TextLine(tokens, baseSize, centered = true)
                } else {
                    val slack = widthPx * SAFETY - naturalWidth(block, range, baseSize, spaceRef)
                    val stretched = addKashida(tokens, slack, tatweelRef * baseSize / REF_PX)
                    PageSlot.TextLine(stretched, baseSize, centered = false)
                }
            }
            textLines.add(lines)
        }

        // ---- تجميع الشرائح بترتيب الصفحة ----
        val slots = ArrayList<PageSlot>()
        val header = ArrayList<PageSlot>()
        var blockIndex = 0
        var versesIndex = 0
        page.blocks.forEachIndexed { index, block ->
            when (block) {
                is PageBlock.SurahTitle -> {
                    val target = if (headerInMargin && index == 0) header else slots
                    target.add(PageSlot.Title(block.surah))
                    if (block.surah.bismillah.isNotBlank()) {
                        val natural = measureRef(block.surah.bismillah) / REF_PX
                        val size = minOf(baseSize, widthPx * 0.92f / natural)
                        target.add(PageSlot.Bismillah(block.surah.bismillah, size))
                    }
                }

                is PageBlock.Verses -> {
                    repeat(groupCounts[versesIndex]) {
                        slots.addAll(textLines[blockIndex])
                        blockIndex++
                    }
                    versesIndex++
                }
            }
        }
        return PageLayout(
            slots = slots,
            slotHeightPx = slotHeight,
            centerVertically = page.number <= 2,
            header = header,
            overflow = overflow
        )
    }

    // ---- الكشيدة (تمطيط الحروف) ----
    private const val TATWEEL = '\u0640'
    private const val JOIN_NEXT = "بتثجحخسشصضطظعغفقكلمنهيئ"

    private fun isMark(c: Char) =
        c in '\u064B'..'\u065F' || c == '\u0670' || c in '\u06D6'..'\u06ED'

    private fun isJoinTarget(c: Char) = (c in '\u0622'..'\u064A') || c == '\u0671'

    /** أماكن مسموح فيها بكشيدة جوه الكلمة (بعد حرف بيتصل بما بعده وقبل حرف بيتصل بيه). */
    private fun kashidaPositions(word: String): List<Int> {
        val result = ArrayList<Int>()
        var i = 0
        while (i < word.length) {
            val c = word[i]
            if (c in JOIN_NEXT) {
                var j = i + 1
                while (j < word.length && isMark(word[j])) j++
                if (j < word.length) {
                    val next = word[j]
                    val lamAlef = c == 'ل' && (next == 'ا' || next == 'أ' || next == 'إ' || next == 'آ')
                    if (isJoinTarget(next) && !lamAlef) result.add(j)
                }
            }
            i++
        }
        return result
    }

    /**
     * بيمط الكلمات بكشيدة بدل ما يوسّع الفراغات. التعديل للعرض بس (النص المخزّن زي ما هو).
     * [slack] الفراغ المتبقي بالبكسل، [tatweelPx] عرض الكشيدة الواحدة.
     */
    private fun addKashida(tokens: List<LineToken>, slack: Float, tatweelPx: Float): List<LineToken> {
        if (tatweelPx <= 1f || slack < tatweelPx) return tokens
        var remaining = (slack * 0.96f / tatweelPx).toInt()
        val positions = tokens.map { token ->
            val word = if (token.markerStart >= 0) token.text.substring(0, token.markerStart - 1) else token.text
            kashidaPositions(word)
        }
        if (positions.all { it.isEmpty() }) return tokens
        val counts = tokens.indices.map { IntArray(positions[it].size) }
        val cap = 10
        var progressed = true
        var pass = 0
        while (remaining > 0 && progressed && pass < cap * 4) {
            progressed = false
            for (t in tokens.indices) {
                if (remaining == 0) break
                val slots = counts[t]
                if (slots.isEmpty()) continue
                // نبدأ من منتصف الكلمة ونتفرّع.
                val slot = (slots.size / 2 + (if (pass % 2 == 0) pass / 2 else -(pass / 2 + 1)))
                    .let { ((it % slots.size) + slots.size) % slots.size }
                if (slots[slot] < cap) {
                    slots[slot]++
                    remaining--
                    progressed = true
                }
            }
            pass++
        }
        return tokens.mapIndexed { t, token ->
            val slots = counts[t]
            if (slots.all { it == 0 }) return@mapIndexed token
            val sb = StringBuilder()
            val pos = positions[t]
            var added = 0
            token.text.forEachIndexed { index, ch ->
                pos.forEachIndexed { s, p -> if (p == index) repeat(slots[s]) { sb.append(TATWEEL); added++ } }
                sb.append(ch)
            }
            LineToken(sb.toString(), token.ayah, if (token.markerStart >= 0) token.markerStart + added else -1)
        }
    }

    private fun naturalWidth(block: Block, range: IntRange, sizePx: Float, spaceRef: Float): Float {
        val k = sizePx / REF_PX
        var sum = 0f
        for (i in range) sum += block.widths[i] * k
        return sum + (range.last - range.first) * spaceRef * k
    }

    /** يوزّع التوكنز على [lineCount] أسطر بحيث مجموع مربعات الفراغ في الأسطر أقل ما يمكن. */
    private fun balance(
        block: Block,
        sizePx: Float,
        lineCount: Int,
        spaceRef: Float,
        widthPx: Float
    ): List<IntRange> {
        val n = block.tokens.size
        val k = sizePx / REF_PX
        val prefix = FloatArray(n + 1)
        for (i in 0 until n) prefix[i + 1] = prefix[i] + block.widths[i] * k
        val space = spaceRef * k
        fun width(from: Int, toExclusive: Int) = prefix[toExclusive] - prefix[from] + (toExclusive - from - 1) * space

        val dp = Array(lineCount + 1) { FloatArray(n + 1) { INF } }
        val from = Array(lineCount + 1) { IntArray(n + 1) { -1 } }
        dp[0][0] = 0f
        for (line in 1..lineCount) {
            for (end in line..n) {
                for (start in (line - 1) until end) {
                    val previous = dp[line - 1][start]
                    if (previous >= INF) continue
                    val w = width(start, end)
                    if (w > widthPx) continue
                    val isLastLine = line == lineCount && end == n
                    val slack = widthPx - w
                    val cost = previous + if (isLastLine && block.endsSurah) 0f else slack * slack
                    if (cost < dp[line][end]) {
                        dp[line][end] = cost
                        from[line][end] = start
                    }
                }
            }
        }
        if (dp[lineCount][n] >= INF) return greedyRanges(block, sizePx, spaceRef, widthPx)

        val result = ArrayList<IntRange>()
        var end = n
        for (line in lineCount downTo 1) {
            val start = from[line][end]
            result.add(start until end)
            end = start
        }
        result.reverse()
        return result
    }

    private fun greedyRanges(block: Block, sizePx: Float, spaceRef: Float, widthPx: Float): List<IntRange> {
        val k = sizePx / REF_PX
        val space = spaceRef * k
        val result = ArrayList<IntRange>()
        var start = 0
        var current = 0f
        for (i in block.tokens.indices) {
            val w = block.widths[i] * k
            if (i == start) {
                current = w
            } else if (current + space + w <= widthPx) {
                current += space + w
            } else {
                result.add(start until i)
                start = i
                current = w
            }
        }
        result.add(start until block.tokens.size)
        return result
    }
}