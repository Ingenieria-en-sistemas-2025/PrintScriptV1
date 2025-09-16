package org.printscript.lexer.tokencreators

import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.lexer.Lexeme
import org.printscript.lexer.error.LexerError
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.Token

object BooleanLiteralCreator : TokenCreator {
    override fun create(lexeme: Lexeme): Result<Token, LexerError> =
        Success(BooleanLiteralToken(lexeme.text == "true", lexeme.span))
}
