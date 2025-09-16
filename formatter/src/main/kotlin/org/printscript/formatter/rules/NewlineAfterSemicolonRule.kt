package org.printscript.formatter.rules

import org.printscript.common.Separator
import org.printscript.token.EofToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class NewlineAfterSemicolonRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) {
            if (current is EofToken) return null
            return "\n"
        }
        return null
    }
}
