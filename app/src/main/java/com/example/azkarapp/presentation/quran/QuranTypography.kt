package com.example.azkarapp.presentation.quran

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalQuranFont = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

val LocalQuranFontIsCustom = staticCompositionLocalOf { false }

@Composable
fun ProvideQuranFont(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val family by produceState<FontFamily?>(QuranFontCache.cached) {
        if (value == null) value = withContext(Dispatchers.IO) { QuranFontCache.load(context) }
    }
    val loaded = family ?: return
    CompositionLocalProvider(
        LocalQuranFont provides loaded,
        LocalQuranFontIsCustom provides (loaded !== FontFamily.Default),
        content = content
    )
}

fun preloadQuranFont(context: Context) {
    QuranFontCache.load(context.applicationContext)
}

private const val QURAN_FONT_WEIGHT = 400

private val QuranFontFiles = listOf("quran.ttf", "quran.otf")

private object QuranFontCache {
    @Volatile
    var cached: FontFamily? = null

    @Synchronized
    fun load(context: Context): FontFamily {
        cached?.let { return it }
        val assets = context.assets
        val installed = runCatching { assets.list("fonts")?.toList().orEmpty() }
            .getOrDefault(emptyList())
        val file = QuranFontFiles.firstOrNull { it in installed }
        val family = if (file == null) {
            FontFamily.Default
        } else {
            runCatching {
                val base = Typeface.createFromAsset(assets, "fonts/$file")
                FontFamily(Typeface.create(base, QURAN_FONT_WEIGHT, false))
            }
                .getOrDefault(FontFamily.Default)
        }
        cached = family
        return family
    }
}

fun arabicIndic(number: Int): String =
    number.toString().map { '٠' + (it - '0') }.joinToString("")


fun ayahMarker(number: Int, customFont: Boolean = true): String =
    if (customFont) "\u06DD${arabicIndic(number)}" else "\uFD3F${arabicIndic(number)}\uFD3E"

private val SpaceBeforeWaqf = Regex(" (?=[\u06D6-\u06DC])")

fun quranDisplayText(raw: String): String = raw.replace(SpaceBeforeWaqf, "")