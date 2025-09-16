package org.printscript.lexer.tokencreators

import org.printscript.common.Result
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.Token

fun interface TokenCreator {
    fun create(lexeme: Lexeme): Result<Token, LexerError>
}
