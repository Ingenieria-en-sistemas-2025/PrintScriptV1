interface TokenStream {
    fun peek(): Result<Token, LabeledError>
    fun next(): Result<Pair<Token, TokenStream>, LabeledError> // Inmutabilidad
    fun isEof(): Boolean
}

// Labeled error -> Para desacoplar errores
