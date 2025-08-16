data class IdentifierToken(val identifier:String, override val span: Span): Token {
    override fun toString() = "ID($identifier)"
}
