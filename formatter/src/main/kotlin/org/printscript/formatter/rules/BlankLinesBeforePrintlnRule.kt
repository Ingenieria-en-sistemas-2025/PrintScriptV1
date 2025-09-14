package org.printscript.formatter.rules

import org.printscript.common.Separator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.EofToken
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class BlankLinesBeforePrintlnRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        // Aplicamos líneas en blanco después de semicolons (asumiendo que separan statements)
        if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) {
            // No agregar líneas si el siguiente token es EOF
            if (current is EofToken) return null

            // No agregar líneas si el siguiente token es RBRACE (fin de bloque)
            if (current is SeparatorToken && current.separator == Separator.RBRACE) return null

            val extra = cfg.blankLinesBeforePrintln
            return if (extra > 0) "\n".repeat(extra) else null
        }
        return null
    }
}
