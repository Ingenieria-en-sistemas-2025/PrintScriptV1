package org.printscript.lexer.lexingrules

import org.printscript.common.Span
import org.printscript.token.NumberLiteralToken
import org.printscript.token.Token

private val NUM_REGEX = Regex("\\d+(?:\\.\\d+)?")

class NumberRule : LexingRule {
    override fun matchLength(input: CharSequence): Int =
        NUM_REGEX.matchAt(input, 0)?.value?.length ?: 0

    override fun build(lexeme: String, span: Span): Token =
        NumberLiteralToken(lexeme, span)
}
