package com.example.azkarapp.domain.recitation

import com.example.azkarapp.domain.model.Ayah
import com.example.azkarapp.domain.util.ArabicNormalizer

enum class WordStatus { PENDING, CORRECT, WRONG }


data class ExpectedWord(
    val display: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val variants: List<String>
)

data class RecitationResult(
    val statuses: List<WordStatus>,
    val consumed: Int,
    val correct: Int,
    val wrong: Int
) {
    val total: Int get() = statuses.size
    val reachedEnd: Boolean get() = total > 0 && consumed >= total
    val correctRatio: Float get() = if (total == 0) 0f else correct.toFloat() / total

    val isPass: Boolean get() = reachedEnd && correctRatio >= RecitationMatcher.PASS_RATIO

    companion object {
        fun empty(total: Int) = RecitationResult(List(total) { WordStatus.PENDING }, 0, 0, 0)
    }
}

object RecitationMatcher {

    const val PASS_RATIO = 0.9f

    fun buildWords(ayahs: List<Ayah>): List<ExpectedWord> {
        val words = ArrayList<ExpectedWord>()
        for (ayah in ayahs) {
            for (token in ayah.text.split(' ', ' ').filter { it.isNotBlank() }) {
                val plain = ArabicNormalizer.normalize(token)
                if (plain.isEmpty() && words.isNotEmpty() && words.last().ayahNumber == ayah.number &&
                    words.last().surahNumber == ayah.surahNumber
                ) {
                    val prev = words.removeAt(words.lastIndex)
                    words.add(prev.copy(display = prev.display + token))
                    continue
                }
                if (plain.isEmpty()) continue
                val variants = listOf(
                    plain,
                    ArabicNormalizer.normalize(token, daggerAsAlef = true),
                    plain.replace("ء", "")
                ).filter { it.isNotEmpty() }.distinct()
                words.add(ExpectedWord(token, ayah.surahNumber, ayah.number, variants))
            }
        }
        return words
    }

    fun match(expected: List<ExpectedWord>, recognizedText: String): RecitationResult {
        val n = expected.size
        val heard = ArabicNormalizer.normalize(recognizedText)
            .split(' ').filter { it.isNotEmpty() }
        val m = heard.size
        if (n == 0 || m == 0) return RecitationResult.empty(n)

        val ok = Array(n) { i -> BooleanArray(m) { j -> wordMatches(expected[i].variants, heard[j]) } }


        val d = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) d[i][0] = i
        for (j in 0..m) d[0][j] = j
        for (i in 1..n) {
            for (j in 1..m) {
                val diag = d[i - 1][j - 1] + if (ok[i - 1][j - 1]) 0 else 1
                d[i][j] = minOf(diag, d[i - 1][j] + 1, d[i][j - 1] + 1)
            }
        }


        var end = 0
        var best = Int.MAX_VALUE
        for (i in 0..n) {
            if (d[i][m] <= best) {
                best = d[i][m]
                end = i
            }
        }

        val statuses = MutableList(n) { WordStatus.PENDING }
        var i = end
        var j = m
        while (i > 0) {
            if (j > 0 && d[i][j] == d[i - 1][j - 1] + if (ok[i - 1][j - 1]) 0 else 1) {
                statuses[i - 1] = if (ok[i - 1][j - 1]) WordStatus.CORRECT else WordStatus.WRONG
                i--; j--
            } else if (d[i][j] == d[i - 1][j] + 1) {
                statuses[i - 1] = WordStatus.WRONG
                i--
            } else {
                j--
            }
        }
        return RecitationResult(
            statuses = statuses,
            consumed = end,
            correct = statuses.count { it == WordStatus.CORRECT },
            wrong = statuses.count { it == WordStatus.WRONG }
        )
    }

    private fun allowedErrors(length: Int): Int = when {
        length <= 3 -> 0
        length <= 7 -> 1
        else -> 2
    }

    private fun wordMatches(variants: List<String>, heard: String): Boolean {
        for (v in variants) {
            val tol = allowedErrors(maxOf(v.length, heard.length))
            if (kotlin.math.abs(v.length - heard.length) > tol) continue
            if (levenshtein(v, heard, tol) <= tol) return true
        }
        return false
    }

    private fun levenshtein(a: String, b: String, limit: Int): Int {
        if (a == b) return 0
        var prev = IntArray(b.length + 1) { it }
        var cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            cur[0] = i
            var rowMin = cur[0]
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
                if (cur[j] < rowMin) rowMin = cur[j]
            }
            if (rowMin > limit) return limit + 1
            val t = prev; prev = cur; cur = t
        }
        return prev[b.length]
    }
}
