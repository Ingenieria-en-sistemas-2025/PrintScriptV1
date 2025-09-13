package org.printscript.runner.runners

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.Analyzing
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerDiagnosticCollector
import org.printscript.runner.RunnerError
import org.printscript.runner.ValidationReport
import org.printscript.runner.hasErrors
import org.printscript.runner.tokenStream

object ValidateRunner : RunningMethod<ValidationReport> {
    override fun run(version: Version, io: ProgramIo): Result<ValidationReport, RunnerError> {
        val w = LanguageWiringFactory.forVersion(version)

        val ts = tokenStream(io, w)
        val stmts = w.statementStreamFromTokens(ts)

        // analyzer
        val emitter = RunnerDiagnosticCollector()
        val cfg = AnalyzerConfig()

        return when (val ar = w.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> {
                val diags = emitter.diagnostics
                Success(ValidationReport(diags, diags.hasErrors()))
            }
            is Failure -> Failure(RunnerError(Analyzing, "analyze error", ar.error))
        }
    }
}
