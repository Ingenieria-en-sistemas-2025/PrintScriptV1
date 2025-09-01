object ParserUtils {

    // Helpers para “exigir” un token:
    // Si el token coincide, consumen y devuelven (tokenTipado, streamAvanzado)
    // Sino, Failure(ParserError(...))

    fun expectIdentifier(ts: TokenStream): Result<Pair<IdentifierToken, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is IdentifierToken) {
                ts.next().map { (t, nextTs) -> (t as IdentifierToken) to nextTs } // consumo y devuelvo (token tipado, stream avanzado)
            } else {
                Failure(LabeledError.of(token.span, "Se esperaba identificador")) // reporto error sin avanzar el stream
            }
        }

    fun expectOperator(ts: TokenStream, operator: Operator): Result<Pair<OperatorToken, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is OperatorToken && token.operator == operator) {
                ts.next().map { (t, nextTs) -> (t as OperatorToken) to nextTs }
            } else {
                Failure(LabeledError.of(token.span, "Se esperaba operador $operator"))
            }
        }

    fun expectSeparator(ts: TokenStream, separator: Separator): Result<Pair<SeparatorToken, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is SeparatorToken && token.separator == separator) {
                ts.next().map { (t, nextTs) -> (t as SeparatorToken) to nextTs }
            } else {
                Failure(LabeledError.of(token.span, "Se esperaba separador $separator"))
            }
        }

    fun expectKeyword(ts: TokenStream, kw: Keyword): Result<Pair<KeywordToken, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is KeywordToken && token.kind == kw) {
                ts.next().map { (t, nextTs) -> (t as KeywordToken) to nextTs }
            } else {
                Failure(LabeledError.of(token.span, "Se esperaba keyword $kw"))
            }
        }

    fun expectTypeToken(ts: TokenStream): Result<Pair<TypeToken, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is TypeToken) {
                ts.next().map { (t, nextTs) -> (t as TypeToken) to nextTs }
            } else {
                Failure(LabeledError.of(token.span, "Se esperaba tipo"))
            }
        }

    fun consumeIfOperator(ts: TokenStream, operator: Operator): Result<Pair<Boolean, TokenStream>, LabeledError> =
        ts.peek().flatMap { token ->
            if (token is OperatorToken && token.operator == operator) {
                ts.next().map { (_, nextTs) -> true to nextTs }
            } else {
                Success(false to ts)
            }
        }
}
