package org.printscript.formatter.rules

import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.Token

class BlankLinesBeforePrintlnRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        // Insertar líneas antes del token println
        if (current is org.printscript.token.KeywordToken && current.kind.string == "println") {
            val extra = cfg.blankLinesAfterPrintln
            if (extra <= 0) return null

            // Si viene justo después de '{', evitá doble separación visual
            if (prev is org.printscript.token.SeparatorToken &&
                prev.separator == org.printscript.common.Separator.LBRACE
            ) {
                // una sola línea es suficiente (o incluso ninguna, elegí)
                return "\n"
            }
            return "\n".repeat(extra) // p.ej. 2
        }
        return null
    }
}
