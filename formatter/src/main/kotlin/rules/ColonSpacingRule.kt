package rules
import Separator
import SeparatorToken
import Token
import config.FormatterOptions

class ColonSpacingRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is SeparatorToken && current.separator == Separator.COLON) {
            return if (cfg.spaceBeforeColonInDecl) " " else ""
        }
        if (prev is SeparatorToken && prev.separator == Separator.COLON) {
            return if (cfg.spaceAfterColonInDecl) " " else ""
        }
        return null
    }
}
