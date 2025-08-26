data class Binary(
    val left: Expression,
    val right: Expression,
    val operator: Operator,
    override val span: Span,
) : Expression
