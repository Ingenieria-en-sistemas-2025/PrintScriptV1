package org.printscript.formatter.rules
import org.printscript.token.Token

interface FormattingRule {
    fun apply(prev: Token?, current: Token, next: Token?): String?
}
