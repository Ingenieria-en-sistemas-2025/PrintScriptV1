package rules

import SeparatorToken
import Token

class NewlineAfterSemicolonRule : FormattingRule {
    override fun apply(prev: Token?, current: Token, next: Token?): String? {
        return if (current is SeparatorToken && current.separator == Separator.SEMICOLON) {
            ";\n" // imprime el ; y fuerza newline
        } else {
            null // no aplica, que decidan otras reglas o el default
        }
    }
}
