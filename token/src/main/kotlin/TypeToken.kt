data class TypeToken(val type: Type, override val span: Span) : WordLikeToken {
    override fun toString() = "TYPE($type)"
}
