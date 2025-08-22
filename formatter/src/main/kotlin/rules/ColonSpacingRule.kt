package rules

import SeparatorToken
import Token
import WordLikeToken
import config.FormatterConfig

class ColonSpacingRule(private val formatterConfig: FormatterConfig) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is SeparatorToken && current.separator == Separator.COLON) {
            val left = if(formatterConfig.spaceBeforeColonInDecl && (prev is WordLikeToken)) " " else ""
            val right = if (formatterConfig.spaceAfterColonInDecl) " " else ""
            return "$left:$right"
        }
        return null
    }
}