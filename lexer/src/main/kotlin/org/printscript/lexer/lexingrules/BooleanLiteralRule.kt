package org.printscript.lexer.lexingrules

import org.printscript.common.Span
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.Token

object BooleanLiteralRule : LexingRule {
    private val re = Regex("(?:true|false)\\b")
    override fun matchLength(input: CharSequence): Int =
        re.matchAt(input, 0)?.value?.length ?: 0

    override fun build(lexeme: String, span: Span): Token =
        BooleanLiteralToken(lexeme == "true", span)
}
