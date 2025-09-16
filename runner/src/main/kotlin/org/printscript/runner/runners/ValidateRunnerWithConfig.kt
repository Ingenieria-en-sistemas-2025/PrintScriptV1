package org.printscript.runner.runners

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.Analyzing
import org.printscript.runner.CappedDiagnosticCollector
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerError
import org.printscript.runner.ValidationReport
import org.printscript.runner.hasErrors
import org.printscript.runner.tokenStream

class ValidateRunnerWithConfig(private val config: AnalyzerConfig) : RunningMethod<ValidationReport> {
    override fun run(version: Version, io: ProgramIo): Result<ValidationReport, RunnerError> {
        val w = LanguageWiringFactory.forVersion(version)
        val ts = tokenStream(io, w)
        val stmts = w.statementStreamFromTokens(ts)

        val emitter = CappedDiagnosticCollector(keepLast = 2000, hardLimit = 100_000)

        return when (val ar = w.analyzer.analyze(stmts, config, emitter)) {
            is Success -> {
                val diags = emitter.snapshot() // sólo últimos N
                val hasErr = emitter.hasErrors()
                Success(ValidationReport(diags, hasErr))
            }
            is Failure -> Failure(RunnerError(Analyzing, "analyze error", ar.error))
        }
    }
}
