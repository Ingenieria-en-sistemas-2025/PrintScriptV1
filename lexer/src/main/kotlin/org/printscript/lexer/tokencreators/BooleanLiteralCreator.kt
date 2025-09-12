package org.printscript.lexer.tokencreators

import org.printscript.lexer.Lexeme
import org.printscript.token.BooleanLiteralToken
import org.printscript.token.Token

object BooleanLiteralCreator : TokenCreator {
    override fun create(lexeme: Lexeme): Token =
        BooleanLiteralToken(lexeme.text == "true", lexeme.span)
}
