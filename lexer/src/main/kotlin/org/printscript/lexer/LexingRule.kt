package org.printscript.lexer

import org.printscript.common.Span
import org.printscript.token.Token

interface LexingRule {

    fun matchLength(string: String): Int
    fun build(lexeme: String, span: Span): Token
}
