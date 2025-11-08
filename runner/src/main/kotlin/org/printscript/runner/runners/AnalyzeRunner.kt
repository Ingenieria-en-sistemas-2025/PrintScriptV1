package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.common.Version
import org.printscript.runner.LanguageWiringFactory
import org.printscript.runner.ProgramIo
import org.printscript.runner.RunnerDiagnosticCollector
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import org.printscript.runner.helpers.AnalyzerConfigResolver
import org.printscript.runner.tokenStream

// Analiza el codigo y devuelve todos los diagnosticos
object AnalyzeRunner : RunningMethod<List<Diagnostic>> {

    override fun run(version: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> {
        val wiring = LanguageWiringFactory.forVersion(version)

        val ts = try { tokenStream(io, wiring) } catch (e: Exception) { return Failure(RunnerError(Stage.Lexing, "lexing failed", e)) }

        val stmts = try { wiring.statementStreamFromTokens(ts) } catch (e: Exception) { return Failure(RunnerError(Stage.Parsing, "parsing failed", e)) }

        val cfg = when (val cfgRes = AnalyzerConfigResolver.fromPathStrict(io.configPath?.toString())) {
            is Success -> cfgRes.value
            is Failure -> return Failure(cfgRes.error)
        }

        val emitter = RunnerDiagnosticCollector()
        return when (val analysisResult = wiring.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> Success(emitter.diagnostics)
            is Failure -> Failure(RunnerError(Stage.Analyzing, "analyze error", analysisResult.error as? Throwable))
        }
    }
}
