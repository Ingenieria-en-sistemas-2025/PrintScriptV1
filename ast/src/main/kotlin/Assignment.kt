data class Assignment(
    val name: String,
    val value: Expression,
    override val span: Span
) : Statement
