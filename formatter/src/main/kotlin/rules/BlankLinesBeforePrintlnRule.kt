package rules

import KeywordToken
import Token
import config.FormatterConfig

class BlankLinesBeforePrintlnRule(private val formatterConfig: FormatterConfig) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is KeywordToken && current.kind == Keyword.PRINTLN) {
            val nlines = formatterConfig.blankLinesBeforePrintln.coerceIn(0, 2) // me aseguro que siempre este en ese rango
            return if (nlines == 0) null else "\n".repeat(nlines) + current.kind.string
        }
        return null
    }
}
