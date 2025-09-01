package rules
import Separator
import SeparatorToken
import Token

class NewlineAfterSemicolonRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        return if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) "\n" else null
    }
}
