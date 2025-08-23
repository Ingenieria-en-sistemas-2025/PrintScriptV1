data class SeparatorToken(val separator: Separator, override val span: Span) : Token {
    override fun toString() = "SEP($separator)"
}
