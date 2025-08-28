class IdentifierStyleRule : Rule {
    override val id: String = "PS-ID-STYLE"

    // ver de alguna manera de inyectar esto.

    private fun isCamel(s: String) = s.matches(Regex("[a-z]+([A-Z][a-z0-9]*)*"))
    private fun isSnake(s: String) = s.matches(Regex("[a-z]+(_[a-z0-9]+)*"))

    override fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic> {
        val out = mutableListOf<Diagnostic>()
        fun ok(name: String) = when (context.config.identifiers.style) {
            IdentifierStyle.CAMEL_CASE -> isCamel(name)
            IdentifierStyle.SNAKE_CASE -> isSnake(name)
        }
        fun visitExpr(e: Expression) {
            when (e) {
                is Variable -> if (!ok(e.name)) {
                    out += Diagnostic(
                        id,
                        "Identificador '${e.name}' no respeta ${context.config.identifiers.style}",
                        (e.span),
                        Severity.WARNING,
                    )
                }
                is Binary -> {
                    visitExpr(e.left)
                    visitExpr(e.right)
                }
                is Grouping -> visitExpr(e.expression)
                is LiteralNumber, is LiteralString -> {}
            }
        }

        for (st in program.statements) {
            when (st) {
                is VarDeclaration -> {
                    if (!ok(st.name)) {
                        out += Diagnostic(
                            id,
                            "Variable '${st.name}' no respeta ${context.config.identifiers.style}",
                            (st).span,
                            Severity.WARNING,
                        )
                    }
                    st.initializer?.let(::visitExpr)
                }
                is Assignment -> {
                    if (!ok(st.name)) {
                        out += Diagnostic(
                            id,
                            "Asignación a '${st.name}' no respeta ${context.config.identifiers.style}",
                            (st).span,
                            Severity.WARNING,
                        )
                    }
                    visitExpr(st.value)
                }
                is Println -> visitExpr(st.value)
            }
        }
        return out
    }
}
