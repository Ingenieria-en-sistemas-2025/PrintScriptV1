class DefaultAnalyzer(private val rules: List<Rule>) : Analyzer {
    override fun analize(program: ProgramNode, config: AnalyzerConfig): Result<DiagnosticReport, LabeledError> = try {
        val context = AnalyzerContext(config)

        val diags = rules
            .flatMap { it.check(program, context) }
            .sortedWith(compareBy({ it.span.start.line }, { it.span.start.column }))
        Success(DiagnosticReport(diags))
    } catch (e: UnsupportedOperationException) {
        failure("Analyzer crashed (unsupported op): ${e.message}")
    }

    private fun failure(msg: String): Failure<LabeledError> =
        Failure(object : LabeledError {
            override val span = Span(Position(1, 1), Position(1, 1))
            override val message = msg
        })
}
