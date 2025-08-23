data class StringLiteralToken(val literal: String, override val span: Span) : WordLikeToken {
    override fun toString() = "STR(\"$literal\")"
}
