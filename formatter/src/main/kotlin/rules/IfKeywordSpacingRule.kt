package rules

import Keyword
import KeywordToken
import Separator
import SeparatorToken
import Token

class IfKeywordSpacingRule : FormattingRule {

    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (isIf(prev) && isLparen(current)) return " "
        if (isRparen(prev) && isLbrace(current)) return " "
        if (isRbrace(prev) && isElse(current)) return " "
        if (isElse(prev) && isLbrace(current)) return " "

        return null
    }

    private fun isIf(t: Token?) = t is KeywordToken && t.kind == Keyword.IF
    private fun isElse(t: Token?) = t is KeywordToken && t.kind == Keyword.ELSE

    private fun isLparen(t: Token?) = t is SeparatorToken && t.separator == Separator.LPAREN
    private fun isRparen(t: Token?) = t is SeparatorToken && t.separator == Separator.RPAREN
    private fun isLbrace(t: Token?) = t is SeparatorToken && t.separator == Separator.LBRACE
    private fun isRbrace(t: Token?) = t is SeparatorToken && t.separator == Separator.RBRACE
}
