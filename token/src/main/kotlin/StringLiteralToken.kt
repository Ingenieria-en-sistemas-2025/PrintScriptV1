data class StringLiteralToken(val literal: String, override val span: Span):Token {
    override fun toString() = "STR(\"$literal\")"
}


