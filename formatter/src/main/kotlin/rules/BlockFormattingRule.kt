package rules

import Separator
import SeparatorToken
import Token

class BlockFormattingRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        // Después de una llave de apertura, agregar salto de línea con indentación
        if (prev is SeparatorToken && prev.separator == Separator.LBRACE) {
            return "\n"
        }

        // Antes de una llave de cierre, agregar salto de línea
        if (current is SeparatorToken && current.separator == Separator.RBRACE) {
            return "\n"
        }

        // Después de punto y coma, agregar salto de línea
        if (prev is SeparatorToken && prev.separator == Separator.SEMICOLON) {
            // Si el siguiente token es una llave de cierre, no agregar indentación extra
            if (next is SeparatorToken && next.separator == Separator.RBRACE) {
                return "\n"
            }
            return "\n"
        }

        return null
    }
}
