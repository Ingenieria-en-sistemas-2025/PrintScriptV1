package org.printscript.formatter.rules

import org.printscript.common.Separator
import org.printscript.token.EofToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class BlockFormattingRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (prev is SeparatorToken && prev.separator == Separator.LBRACE) {
            if (current is SeparatorToken && current.separator == Separator.RBRACE) return null
            return "\n"
        }
        if (current is SeparatorToken && current.separator == Separator.RBRACE) {
            if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) return null
            if (next is EofToken) return null
            return "\n"
        }
        return null
    }
}
