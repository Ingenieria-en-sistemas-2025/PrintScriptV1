package org.printscript.formatter.rules

import org.printscript.common.Keyword
import org.printscript.common.Operator
import org.printscript.common.Separator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.IdentifierToken
import org.printscript.token.KeywordToken
import org.printscript.token.OperatorToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class MandatorySpacingRule(private val config: FormatterOptions) : FormattingRule {

    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (config.mandatorySingleSpaceSeparation != true) return null
        return if (needsSpace(prev, current)) " " else null
    }
    private fun needsSpace(prev: Token?, current: Token): Boolean = when {
        // Antes de :
        isColon(current) && prev is IdentifierToken -> true
        // Antes de =
        isAssign(current) -> true
        // Antes de (
        isLparen(current) && (isPrintln(prev) || isIf(prev)) -> true
        // Antes de )
        isRparen(current) -> true
        // Post :
        isColon(prev) -> true
        // Post =
        isAssign(prev) -> true
        // Post (
        isLparen(prev) -> true
        // Post de keywords (let, const, println)
        isKeywordNeedingSpace(prev) -> true
        else -> false
    }

    private fun isColon(t: Token?) =
        t is SeparatorToken && t.separator == Separator.COLON

    private fun isLparen(t: Token?) =
        t is SeparatorToken && t.separator == Separator.LPAREN

    private fun isRparen(t: Token?) =
        t is SeparatorToken && t.separator == Separator.RPAREN

    private fun isAssign(t: Token?) =
        t is OperatorToken && t.operator == Operator.ASSIGN

    private fun isPrintln(t: Token?) =
        t is IdentifierToken && t.identifier == "println"

    private fun isIf(t: Token?) =
        t is KeywordToken && t.kind == Keyword.IF

    private fun isKeywordNeedingSpace(t: Token?): Boolean {
        if (t !is KeywordToken) return false
        return when (t.kind) {
            Keyword.LET, Keyword.CONST, Keyword.PRINTLN -> true
            else -> false
        }
    }
}
