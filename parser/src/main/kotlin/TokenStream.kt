
class TokenStream(private val tokens : List<Token>) {
    private var i = 0
    fun peek(lookahead: Int = 0) : Token{
        return tokens.getOrElse(i+lookahead){EofToken()} // devuelve EOF si no hay más tokens
    }
    fun next(): Token{
        val actual: Token = peek()
        if(i < tokens.size){
            i += 1
        }
        return actual
    }
    fun isAtEnd(): Boolean{
        return peek() is EofToken
    }

    // Verificadores: que el proximo token coincida con la gramática
    // (keyword/separador/operador). Si coincide, lo consumen (next())
    // y lo devuelven tipado. Si no, devuelve error.
    fun expectKeyword(expectedKeyword : Keyword) : KeywordToken{
        val nextToken = peek()
        if(nextToken is KeywordToken && nextToken.kind == expectedKeyword){
            next()
            return nextToken
        }
        error("Se esperaba keyword $expectedKeyword, encontrado: $nextToken")
    }

    fun expectSep(expectedSeparator: Separator): SeparatorToken {
        val nextToken = peek()
        if (nextToken is SeparatorToken && nextToken.separator == expectedSeparator) {
            next()
            return nextToken
        }
        error("Se esperaba separador $expectedSeparator, encontrado: $nextToken")
    }

    fun expectOp(expectedOperator: Operator): OperatorToken {
        val nextToken = peek()
        if (nextToken is OperatorToken && nextToken.operator == expectedOperator) {
            next()
            return nextToken
        }
        error("Se esperaba operador $expectedOperator, encontrado: $nextToken")
    }

    fun expectIdentifier(): IdentifierToken{
        val nextToken = peek()
        if (nextToken is IdentifierToken){
            next()
            return nextToken
        }
        error("Se esperaba un identificador, pero llegó: $nextToken")
    }

    fun expectTypeToken(): TypeToken {
        val next = peek()
        if (next is TypeToken) {
            next()
            return next
        }
        error("Se esperaba un tipo, llegó: $next")
    }

    fun consumeIfOperator(expectedOp : Operator) : Boolean{
        val next = peek()
        val isExpected = next is OperatorToken && next.operator == expectedOp
        if(isExpected){
            next() // consume si coincide
            return true
        }
        return false
    }

}