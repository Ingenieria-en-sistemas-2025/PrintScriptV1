data class ReadEnv(
    val variableName: Expression, // Literal String
    override val span: Span,
) : Expression
