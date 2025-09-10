package org.printscript.lexer.lexingrules

import org.printscript.common.Span
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token

object StringRule : LexingRule {

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

    override fun build(lexeme: String, span: Span): Token {
        val content = extractInnerContent(lexeme)
        return StringLiteralToken(content, span)
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

    private fun extractInnerContent(lexeme: String): String =
        lexeme.substring(1, lexeme.length - 1)
}
