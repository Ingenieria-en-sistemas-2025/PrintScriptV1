data class KeywordToken(val kind: Keyword, override val span: Span) : Token {
    override fun toString() = "KW($kind)"
}
