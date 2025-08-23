data class UnexpectedChar(override val span: Span, val char: Char) : LexerError {
    override val message = "Símbolo inesperado: '$char'"
}
