package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.Severity
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.tokenStream

object ParseRunner : RunningMethod<List<Diagnostic>> {
    override fun run(version: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> = try {
        val wiring = LanguageWiringFactory.forVersion(version)

        val ts = try {
            tokenStream(io, wiring)
        } catch (e: Exception) {
            return Failure(RunnerError(Stage.Lexing, "lexing failed", e))
        }

        val stmts = try {
            wiring.statementStreamFromTokens(ts)
        } catch (e: Exception) {
            return Failure(RunnerError(Stage.Parsing, "parsing failed", e))
        }

        val diags = mutableListOf<Diagnostic>()

        tailrec fun loop(cur: StatementStream) {
            when (val step = cur.nextStep()) {
                is Step.Item -> loop(step.next)
                is Step.Error -> {
                    diags += labeledErrorToDiagnostic(step.error)
                    loop(step.next)
                }
                is Step.Eof -> return
            }
        }

        loop(stmts)
        Success(diags)
    } catch (e: Exception) {
        Failure(RunnerError(Stage.Parsing, "unexpected parse failure", e))
    }

    private fun labeledErrorToDiagnostic(error: LabeledError): Diagnostic =
        Diagnostic(
            ruleId = "PS-SYNTAX",
            message = error.message,
            span = error.span,
            severity = Severity.ERROR,
        )
}
