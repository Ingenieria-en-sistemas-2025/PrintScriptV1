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
    @Suppress("ReturnCount")
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (!config.mandatorySingleSpaceSeparation) return null

        // 1. Antes de ':'
        if (current is SeparatorToken && current.separator == Separator.COLON) {
            if (prev is IdentifierToken) {
                return " "
            }
        }

        // 2. Antes de '='
        if (current is OperatorToken && current.operator == Operator.ASSIGN) {
            return " "
        }

        // 3. Antes de '(' después de println, if, etc.
        if (current is SeparatorToken && current.separator == Separator.LPAREN) {
            if (prev is IdentifierToken && prev.identifier == "println") {
                return " "
            }
            if (prev is KeywordToken && prev.kind == Keyword.IF) {
                return " "
            }
        }

        // 4. Antes de ')'
        if (current is SeparatorToken && current.separator == Separator.RPAREN) {
            return " "
        }

        // 5. Después de ':' en declaraciones de tipo
        if (prev is SeparatorToken && prev.separator == Separator.COLON) {
            return " "
        }

        // 6. Después de '=' (operadores de asignación)
        if (prev is OperatorToken && prev.operator == Operator.ASSIGN) {
            return " "
        }

        // 7. Después de '('
        if (prev is SeparatorToken && prev.separator == Separator.LPAREN) {
            return " "
        }

        // 8. Después de keywords como 'let'
        if (prev is KeywordToken) {
            when (prev.kind) {
                Keyword.LET, Keyword.CONST, Keyword.PRINTLN -> return " "
                else -> {}
            }
        }
        return null
    }
}
