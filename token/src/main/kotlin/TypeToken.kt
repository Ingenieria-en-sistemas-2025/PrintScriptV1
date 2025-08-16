data class TypeToken(val type: Type,  override val span: Span) : Token {
    override fun toString() = "TYPE($type)"
}
