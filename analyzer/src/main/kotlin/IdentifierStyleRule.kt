class IdentifierStyleRule(private val conventionProvider: (IdentifiersConfig) -> NameConvention = { IdentifierNaming.from(it) }) : Rule {
    override val id: String = "PS-ID-STYLE"

    override fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic> {
        val config = context.config.identifiers
        val convProvider = conventionProvider(config)
        val severity = if (config.failOnViolation) Severity.ERROR else Severity.WARNING
        val diags = mutableListOf<Diagnostic>()
        val reported = mutableSetOf<Pair<String, Span>>() // evita duplicados exactos

        fun report(name: String, span: Span) {
            val key = name to span
            if (reported.add(key)) {
                diags += Diagnostic(id, "Identificador '$name' no respeta ${convProvider.id}", span, severity)
            }
        }

        fun visitExpr(e: Expression) {
            when (e) {
                is Variable -> if (!convProvider.matches(e.name)) report(e.name, e.span)
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
                    if (!convProvider.matches(st.name)) report(st.name, st.span)
                    if (config.checkReferences) st.initializer?.let(::visitExpr)
                }
                is Assignment -> {
                    if (!convProvider.matches(st.name)) report(st.name, st.span)
                    if (config.checkReferences) visitExpr(st.value)
                }
                is Println -> {
                    if (config.checkReferences) visitExpr(st.value)
                }
            }
        }
        return diags
    }
}
