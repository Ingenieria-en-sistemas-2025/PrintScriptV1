package org.printscript.formatter.rules

import org.printscript.common.Keyword
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.KeywordToken
import org.printscript.token.Token

class BlankLinesBeforePrintlnRule(private val opts: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is KeywordToken && current.kind == Keyword.PRINTLN) {
            val extra = opts.blankLinesAfterPrintln ?: 0
            if (extra <= 0) return null
            return "\n".repeat(extra + 1)
        }
        return null
    }
}
