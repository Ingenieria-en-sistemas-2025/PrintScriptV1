interface TokenStream {
    fun peek(): Result<Token, LabeledError>
    fun next(): Result<Pair<Token, TokenStream>, LabeledError> // Con nuevo token stream porq cambia el indice e inmutabilidad
    fun isEof(): Boolean
}

// Labeled error -> Para desacoplar errores