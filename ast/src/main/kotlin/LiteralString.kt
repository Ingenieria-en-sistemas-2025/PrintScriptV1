
data class LiteralString(
    val value: String,
    override val span: Span,
) : Expression
