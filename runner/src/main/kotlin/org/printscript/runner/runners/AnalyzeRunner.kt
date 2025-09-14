package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
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
import org.printscript.runner.tokenStream

object AnalyzeRunner : RunningMethod<List<Diagnostic>> {
    override fun run(version: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> {
        val w = LanguageWiringFactory.forVersion(version)

        val ts = tokenStream(io, w)
        val stmts = w.statementStreamFromTokens(ts)

        val emitter = RunnerDiagnosticCollector()
        val cfg = AnalyzerConfig()

        return when (val r = w.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> Success(emitter.diagnostics)
            is Failure -> Failure(RunnerError(Analyzing, "analyze error", r.error))
        }
    }
}
