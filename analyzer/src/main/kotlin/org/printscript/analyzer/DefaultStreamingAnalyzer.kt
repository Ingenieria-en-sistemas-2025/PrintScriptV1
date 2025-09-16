package org.printscript.analyzer

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.AnalyzerContext
import org.printscript.analyzer.rules.StreamingRule
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success

class DefaultStreamingAnalyzer(private val rules: List<StreamingRule>) : StreamingAnalyzer {

    @Suppress("TooGenericExceptionCaught")
    override fun analyze(
        stream: StatementStream,
        config: AnalyzerConfig,
        out: DiagnosticEmitter,
    ): Result<Unit, LabeledError> =
        try {
            val context = AnalyzerContext(config)
            loop(stream, context, out)
        } catch (e: UnsupportedOperationException) {
            Failure(
                LabeledError.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Analyzer crashed (unsupported op): ${e.message}",
                ),
            )
        } catch (e: TooManyDiagnosticsException) {
            // devolvemos Failure con un LabeledError claro
            Failure(
                LabeledError.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Analyzer aborted: demasiados diagnósticos (> ${e.maxErrors}). " +
                        "Emisión total: ${e.total}.",
                ),
            )
        } catch (e: Exception) {
            Failure(
                LabeledError.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Analyzer crashed: ${e::class.simpleName}: ${e.message}",
                ),
            )
        }

    private tailrec fun loop(current: StatementStream, context: AnalyzerContext, out: DiagnosticEmitter): Result<Unit, LabeledError> =
        when (val step = current.nextStep()) {
            is Step.Item -> {
                rules.forEach { it.onStatement(step.statement, context, out) }
                loop(step.next, context, out) // tail call
            }
            is Step.Error -> {
                out.report(Diagnostic("PS-SYNTAX", step.error.message, step.error.span, Severity.ERROR))
                loop(step.next, context, out)
            }
            is Step.Eof -> {
                rules.forEach { it.onFinish(context, out) }
                Success(Unit)
            }
        }
}
