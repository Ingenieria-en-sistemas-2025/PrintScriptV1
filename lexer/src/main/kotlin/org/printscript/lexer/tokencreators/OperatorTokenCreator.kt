package org.printscript.lexer.tokencreators

import org.printscript.common.Operator
import org.printscript.lexer.Lexeme
import org.printscript.token.OperatorToken
import org.printscript.token.Token

class OperatorTokenCreator(
    private val ops: Map<String, Operator>,
) : TokenCreator {
    override fun create(lexeme: Lexeme): Token {
        val op = ops[lexeme.text]
        checkNotNull(op) { "internal: OperatorRule produced unknown lexeme '${lexeme.text}'" }
        return OperatorToken(op, lexeme.span)
    }
}
