package org.printscript.formatter.rules

import org.printscript.common.Separator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class IfBraceNewlineRule(private val config: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is SeparatorToken && current.separator == Separator.LBRACE) {
            if (prev is SeparatorToken && prev.separator == Separator.RPAREN) {
                return when {
                    config.ifBraceBelowLine -> "\n"
                    config.ifBraceSameLine -> " "
                    else -> null
                }
            }
        }

        return null
    }
}
