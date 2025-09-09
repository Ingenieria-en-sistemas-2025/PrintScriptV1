package org.printscript.formatter.rules
import org.printscript.token.Token
import org.printscript.token.WordLikeToken

class WordSpacingRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? =
        if (prev is WordLikeToken && current is WordLikeToken) " " else null
}
