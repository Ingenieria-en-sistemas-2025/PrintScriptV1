interface TokenStream {
    fun peek(n: Int = 0): Result<Token, LabeledError>
    fun next(): Result<Pair<Token, TokenStream>, LabeledError> // Inmutabilidad
    fun isEof(): Boolean
}

// Labeled error -> Para desacoplar errores
