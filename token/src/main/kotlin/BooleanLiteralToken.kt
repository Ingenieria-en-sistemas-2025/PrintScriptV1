data class BooleanLiteralToken(val value: Boolean, override val span: Span) : Token {
    override fun toString() = "BOOL($value)"
}
