package org.printscript.lexer.tokencreators

import org.printscript.lexer.Lexeme
import org.printscript.token.NumberLiteralToken
import org.printscript.token.Token

object NumberTokenCreator : TokenCreator {
    override fun create(lexeme: Lexeme): Token =
        NumberLiteralToken(lexeme.text, lexeme.span)
}
