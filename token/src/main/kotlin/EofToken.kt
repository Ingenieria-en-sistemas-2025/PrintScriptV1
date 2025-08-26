data class EofToken(override val span: Span) : Token {
    override fun toString() = "EOF"
}
