package org.printscript.formatter.rules

import org.printscript.common.Separator
import org.printscript.formatter.config.FormatterOptions
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class MandatorySpacingRule(private val cfg: FormatterOptions) : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        if (!cfg.mandatorySingleSpaceSeparation) return null
        // Solo aplicamos si hay un token anterior
        if (prev == null) return null
        // No agregamos espacios después de ciertos separadores que ya manejan su propio espaciado
        if (prev is SeparatorToken) {
            when (prev.separator) {
                Separator.LBRACE -> return null // { ya maneja su espaciado
                Separator.SEMICOLON -> return null // ; ya maneja su espaciado
                Separator.COLON -> return null // : ya maneja su espaciado
                else -> {}
            }
        }

        // No agregamos espacios antes de ciertos separadores
        if (current is SeparatorToken) {
            when (current.separator) {
                Separator.RBRACE -> return null // } ya maneja su espaciado
                Separator.SEMICOLON -> return null // ; ya maneja su espaciado
                else -> {}
            }
        }
        return " "
    }
}
