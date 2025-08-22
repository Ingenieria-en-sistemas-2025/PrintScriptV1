data class KeywordToken(val kind: Keyword, override val span: Span) : WordLikeToken {
    override fun toString() = "KW($kind)"
}
