package org.printscript.lexer.lexingrules

import org.printscript.common.Span
import org.printscript.token.Token

interface LexingRule {

    fun matchLength(input: CharSequence): Int
    fun build(lexeme: String, span: Span): Token
}
