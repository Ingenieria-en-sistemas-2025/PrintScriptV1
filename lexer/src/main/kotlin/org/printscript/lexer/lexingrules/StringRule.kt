package org.printscript.lexer.lexingrules

object StringRule : LexingRule {

    override val key: RuleKey = RuleKey("STRING")

    private const val ESCAPE = '\\'
    private const val QUOTE_DOUBLE = '"'
    private const val QUOTE_SINGLE = '\''

    override fun matchLength(input: CharSequence): Int {
        if (input.isEmpty()) return 0

        val opening = input.first()
        if (!isQuote(opening)) return 0

        val end = findClosingQuoteIndex(input, opening)
        return if (end == -1) 0 else end + 1 // +1 para incluir la comilla de cierre
    }

    private fun isQuote(c: Char): Boolean =
        c == QUOTE_DOUBLE || c == QUOTE_SINGLE

    private fun findClosingQuoteIndex(src: CharSequence, opening: Char): Int {
        var i = 1
        while (i < src.length) {
            val c = src[i]
            if (c == ESCAPE) {
                // Salteamos el carácter escapado (si existe)
                i = skipEscaped(src, i)
                continue
            }
            if (c == opening) return i
            i++
        }
        return -1
    }

    private fun skipEscaped(src: CharSequence, backslashIndex: Int): Int {
        val next = backslashIndex + 1
        return if (next < src.length) next + 1 else next
    }
}
