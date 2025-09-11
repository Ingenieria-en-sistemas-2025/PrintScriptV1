package org.printscript.lexer.tokencreators

import org.printscript.common.Separator
import org.printscript.lexer.Lexeme
import org.printscript.token.SeparatorToken
import org.printscript.token.Token

class SeparatorTokenCreator(private val seps: Map<String, Separator>) : TokenCreator {
    override fun create(lexeme: Lexeme): Token =
        SeparatorToken(
            seps[lexeme.text] ?: error("Separador desconocido: '${lexeme.text}'"),
            lexeme.span,
        )
}
