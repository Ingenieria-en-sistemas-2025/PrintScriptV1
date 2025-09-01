package rules
import Token
import WordLikeToken

class WordSpacingRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? =
        if (prev is WordLikeToken && current is WordLikeToken) " " else null
}
