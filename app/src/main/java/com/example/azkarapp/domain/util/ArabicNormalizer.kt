package com.example.azkarapp.domain.util


object ArabicNormalizer {

    private val WAW_DAGGER_BEFORE_TA = Regex("و[\u064B-\u065F]*\u0670(?=[\u064B-\u065F]*ة)")


    fun normalize(rawInput: String, daggerAsAlef: Boolean = false): String {
        val input = if (daggerAsAlef) WAW_DAGGER_BEFORE_TA.replace(rawInput, "ا") else rawInput
        val sb = StringBuilder(input.length)
        for (c in input) {
            val code = c.code
            when {
                code == 0x0670 -> if (daggerAsAlef) sb.append('ا')
                code in 0x0610..0x061A -> Unit
                code in 0x064B..0x065F -> Unit
                code == 0x0640 -> Unit
                code in 0x06D6..0x06ED -> Unit
                code in 0x08D3..0x08FF -> Unit
                c == 'أ' || c == 'إ' || c == 'آ' || c == 'ٱ' -> sb.append('ا')
                c == 'ى' || c == 'ئ' || c == 'ی' -> sb.append('ي')
                c == 'ؤ' -> sb.append('و')
                c == 'ة' -> sb.append('ه')
                c == 'ک' -> sb.append('ك')
                c.isLetter() || c == ' ' -> sb.append(c)
                else -> Unit
            }
        }
        return sb.toString().trim().replace(Regex("\\s+"), " ")
    }
}
