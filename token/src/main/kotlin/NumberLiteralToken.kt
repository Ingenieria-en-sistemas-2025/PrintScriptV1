data class NumberLiteralToken(val raw: String, override val span: Span): WordLikeToken{
    override fun toString() = "NUM($raw)"
}
