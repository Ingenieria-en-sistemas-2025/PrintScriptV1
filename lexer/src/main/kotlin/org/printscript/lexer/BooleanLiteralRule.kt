package org.printscript.lexer

import org.printscript.common.Span
import org.printscript.token.BooleanLiteralToken

object BooleanLiteralRule : LexingRule {
    private val re = Regex("(?:true|false)\\b")
    override fun matchLength(s: String) = re.matchAt(s, 0)?.value?.length ?: 0
    override fun build(lex: String, span: Span) =
        BooleanLiteralToken(lex == "true", span)
}
