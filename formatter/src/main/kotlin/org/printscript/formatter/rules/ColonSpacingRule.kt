package org.printscript.formatter.rules
import org.printscript.common.Separator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class ColonSpacingRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is SeparatorToken && current.separator == Separator.COLON) {
            return when (cfg.spaceBeforeColonInDecl) {
                true -> " "
                false -> ""
                null -> null
            }
        }

        if (prev is SeparatorToken && prev.separator == Separator.COLON) {
            return when (cfg.spaceAfterColonInDecl) {
                true -> " "
                false -> ""
                null -> null
            }
        }

        return null
    }
}
