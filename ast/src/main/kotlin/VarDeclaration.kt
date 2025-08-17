data class VarDeclaration(
    val name : String,
    val type : Type,
    val initializer : Expression?
) : Statement
