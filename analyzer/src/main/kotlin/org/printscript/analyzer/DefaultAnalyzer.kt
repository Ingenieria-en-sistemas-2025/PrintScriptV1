package org.printscript.analyzer

import org.printscript.ast.ProgramNode
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success

class DefaultAnalyzer(private val rules: List<Rule>) : Analyzer {

    override fun analyze(program: ProgramNode, config: AnalyzerConfig): Result<DiagnosticReport, LabeledError> = try {
        val context = AnalyzerContext(config)

        val diags = rules
            .flatMap { it.check(program, context) }
            .sortedWith(compareBy({ it.span.start.line }, { it.span.start.column }))
        Success(DiagnosticReport(diags))
    } catch (e: UnsupportedOperationException) {
        failure("org.printscript.analyzer.Analyzer crashed (unsupported op): ${e.message}")
    }

    private fun failure(msg: String): Failure<LabeledError> =
        Failure(object : LabeledError {
            override val span = Span(Position(1, 1), Position(1, 1))
            override val message = msg
        })
}
