
data class Variable(
    val name : String,
    override val span: Span
) : Expression
