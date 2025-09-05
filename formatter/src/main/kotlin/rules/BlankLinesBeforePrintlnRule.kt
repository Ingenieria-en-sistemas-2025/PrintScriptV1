package rules
import Keyword
import KeywordToken
import Separator
import SeparatorToken
import Token
import config.FormatterOptions

class BlankLinesBeforePrintlnRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (current is KeywordToken && current.kind == Keyword.PRINTLN) {
            // evita agregar lineas en blanco si es el ppio del archivo
            if (prev == null) return null
            val total = computeLinesToAdd(prev)
            return if (total > 0) "\n".repeat(total) else null
        }
        return null
    }

    private fun computeLinesToAdd(prev: Token): Int {
        val base = if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) 1 else 0
        val extra = cfg.blankLinesBeforePrintln
        val total = base + extra
        return total
    }
}
