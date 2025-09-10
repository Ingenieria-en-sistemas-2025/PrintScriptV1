package expr

import BooleanLiteralToken
import IdentifierToken
import NumberLiteralToken
import StringLiteralToken
import Token

enum class TokenKind { NUMBER, STRING, IDENT, BOOLEAN }

internal fun classifyKind(t: Token): TokenKind? = when (t) {
    is NumberLiteralToken -> TokenKind.NUMBER
    is StringLiteralToken -> TokenKind.STRING
    is IdentifierToken -> TokenKind.IDENT
    is BooleanLiteralToken -> TokenKind.BOOLEAN
    else -> null
}
