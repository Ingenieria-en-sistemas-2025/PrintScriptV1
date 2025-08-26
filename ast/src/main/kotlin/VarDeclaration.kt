data class VarDeclaration(
    val name: String,
    val type: Type,
    val initializer: Expression?,
    override val span: Span,
) : Statement
