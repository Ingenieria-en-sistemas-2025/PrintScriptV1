data class ReadInput(
    val prompt: Expression, // Literal String o variable
    override val span: Span,
) : Expression
