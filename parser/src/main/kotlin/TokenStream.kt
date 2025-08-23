
class TokenStream(private val tokens: List<Token>) {
    private var i = 0

    fun peek(lookahead: Int = 0): Token {
        val idx = i + lookahead
        return if (idx < tokens.size) {
            tokens[idx]
        } else {
            val eofPos: Position =
                if (tokens.isNotEmpty()) {
                    tokens.last().span.end
                } else {
                    Position(1, 1)
                }
            EofToken(Span(eofPos, eofPos))
        }
    }

    fun next(): Token {
        val actual: Token = peek()
        if (i < tokens.size) {
            i += 1
        }
        return actual
    }

    fun isAtEnd(): Boolean {
        return peek() is EofToken
    }

    // Verificadores: que el proximo token coincida con la gramática
    // (keyword/separador/operador). Si coincide, lo consumen (next())
    // y lo devuelven tipado. Si no, devuelve error.

    private fun fail(expected: String, got: Token): Nothing {
        throw ParseError(got.span, "Se esperaba $expected, encontrado $got")
    }

    fun expectKeyword(expectedKeyword: Keyword): KeywordToken {
        val nextToken = peek()
        if (nextToken is KeywordToken && nextToken.kind == expectedKeyword) {
            next()
            return nextToken
        }
        fail("keyword $expectedKeyword", nextToken)
    }

    fun expectSep(expectedSeparator: Separator): SeparatorToken {
        val nextToken = peek()
        if (nextToken is SeparatorToken && nextToken.separator == expectedSeparator) {
            next()
            return nextToken
        }
        fail("separador $expectedSeparator", nextToken)
    }

    fun expectOp(expectedOperator: Operator): OperatorToken {
        val nextToken = peek()
        if (nextToken is OperatorToken && nextToken.operator == expectedOperator) {
            next()
            return nextToken
        }
        fail("operador $expectedOperator", nextToken)
    }

    fun expectIdentifier(): IdentifierToken {
        val nextToken = peek()
        if (nextToken is IdentifierToken) {
            next()
            return nextToken
        }
        fail("identificador", nextToken)
    }

    fun expectTypeToken(): TypeToken {
        val next = peek()
        if (next is TypeToken) {
            next()
            return next
        }
        fail("tipo", next)
    }

    fun consumeIfOperator(expectedOp: Operator): Boolean {
        val next = peek()
        val isExpected = next is OperatorToken && next.operator == expectedOp
        if (isExpected) {
            next() // consume si coincide
            return true
        }
        return false
    }
}
