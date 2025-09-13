package org.printscript.lexer.tokencreators

import org.printscript.common.Separator
import org.printscript.lexer.Lexeme
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class SeparatorTokenCreator(private val seps: Map<String, Separator>) : TokenCreator {
    override fun create(lexeme: Lexeme): Token {
        val sep = seps[lexeme.text]
        checkNotNull(sep) { "internal: SeparatorRule produced unknown lexeme '${lexeme.text}'" }
        return SeparatorToken(sep, lexeme.span)
    }
}
