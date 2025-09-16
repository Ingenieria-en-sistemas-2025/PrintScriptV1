package org.printscript.lexer.tokencreators

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.NumberLiteralToken
import org.printscript.token.Token

object NumberTokenCreator : TokenCreator {
    override fun create(lexeme: Lexeme): Result<Token, LexerError> =
        Success(NumberLiteralToken(lexeme.text, lexeme.span))
}
