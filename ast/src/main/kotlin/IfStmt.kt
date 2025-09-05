data class IfStmt(
    val condition: Expression,
    val thenBranch: List<Statement>,
    val elseBranch: List<Statement>?, // null si no hay else
    override val span: Span,
) : Statement
