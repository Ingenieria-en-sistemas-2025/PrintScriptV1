data class IdentifierToken(val identifier: String, override val span: Span) : WordLikeToken {
    override fun toString() = "ID($identifier)"
}
