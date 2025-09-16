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

// analiza el codigo y devuelve todos los diagnosticos
object AnalyzeRunner : RunningMethod<List<Diagnostic>> {
    override fun run(version: Version, io: ProgramIo): Result<List<Diagnostic>, RunnerError> {
        val wiring = LanguageWiringFactory.forVersion(version)

        // tokenizar el reader de io y obtener un stream de statements
        val ts = tokenStream(io, wiring)
        val stmts = wiring.statementStreamFromTokens(ts)

        val emitter = RunnerDiagnosticCollector()
        val defaultAnalyzerConfig = AnalyzerConfig() // Config por defecto para el analyzer

        return when (val analysisResult = wiring.analyzer.analyze(stmts, defaultAnalyzerConfig, emitter)) {
            is Success -> Success(emitter.diagnostics) // devuelvo disgnosticos reunidos
            is Failure -> Failure(RunnerError(Analyzing, "analyze error", analysisResult.error))
        }
    }
}
