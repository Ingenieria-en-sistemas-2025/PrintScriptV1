package org.printscript.runner.runners

import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerDiagnosticCollector
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.ValidationReport
import org.printscript.runner.hasErrors
import org.printscript.runner.helpers.AnalyzerConfigResolver
import org.printscript.runner.tokenStream

class ValidateRunner : RunningMethod<ValidationReport> {

    override fun run(version: Version, io: ProgramIo): Result<ValidationReport, RunnerError> = try {
        val w = LanguageWiringFactory.forVersion(version)

        val ts = try { tokenStream(io, w) } catch (t: Throwable) { return Failure(RunnerError(Stage.Lexing, "lexing failed", t)) }

        val stmts = try { w.statementStreamFromTokens(ts) } catch (t: Throwable) { return Failure(RunnerError(Stage.Parsing, "parsing failed", t)) }

        val emitter = RunnerDiagnosticCollector()

        val cfg = when (val cfgRes = AnalyzerConfigResolver.fromPathStrict(io.configPath?.toString())) {
            is Success -> cfgRes.value
            is Failure -> return Failure(cfgRes.error)
        }

        when (val ar = w.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> {
                val diags = emitter.diagnostics
                Success(ValidationReport(diags, diags.hasErrors()))
            }
            is Failure -> Failure(RunnerError(Stage.Analyzing, "analyze error", ar.error as? Throwable))
        }
    } catch (t: Throwable) {
        Failure(RunnerError(Stage.Analyzing, "unexpected validate failure", t))
    }
}
