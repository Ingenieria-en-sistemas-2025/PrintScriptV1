data class NumberLiteralToken(val raw: String, override val span: Span):Token{
    override fun toString() = "NUM($raw)"
}
