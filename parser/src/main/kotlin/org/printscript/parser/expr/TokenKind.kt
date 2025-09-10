package org.printscript.parser.expr

import org.printscript.token.BooleanLiteralToken
import org.printscript.token.IdentifierToken
import org.printscript.token.NumberLiteralToken
import org.printscript.token.StringLiteralToken
import org.printscript.token.Token

enum class TokenKind { NUMBER, STRING, IDENT, BOOLEAN }

internal fun classifyKind(t: Token): TokenKind? = when (t) {
    is NumberLiteralToken -> TokenKind.NUMBER
    is StringLiteralToken -> TokenKind.STRING
    is IdentifierToken -> TokenKind.IDENT
    is BooleanLiteralToken -> TokenKind.BOOLEAN
    else -> null
}
