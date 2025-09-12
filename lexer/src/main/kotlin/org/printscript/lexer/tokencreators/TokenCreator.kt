package org.printscript.lexer.tokencreators

import org.printscript.lexer.Lexeme
import org.printscript.token.Token

fun interface TokenCreator {
    fun create(lexeme: Lexeme): Token
}
