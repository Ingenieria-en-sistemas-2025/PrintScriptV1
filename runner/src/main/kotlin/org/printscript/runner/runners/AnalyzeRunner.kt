package org.printscript.runner.runners

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.loader.ConfigFormat
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

        val ts = tokenStream(io, wiring)
        val stmts = wiring.statementStreamFromTokens(ts)

        val cfg = when (val cfgRes = loadAnalyzerConfig(io)) {
            is Success -> cfgRes.value
            is Failure -> return Failure(cfgRes.error)
        }

        val emitter = RunnerDiagnosticCollector()
        return when (val analysisResult = wiring.analyzer.analyze(stmts, cfg, emitter)) {
            is Success -> Success(emitter.diagnostics) // devuelvo diagnosticos reunidos
            is Failure -> Failure(RunnerError(Analyzing, "analyze error", analysisResult.error))
        }
    }

    private fun loadAnalyzerConfig(io: ProgramIo): Result<AnalyzerConfig, RunnerError> {
        val path = io.configPath ?: return Success(AnalyzerConfig())

        val file = path.toFile()
        if (!file.exists() || !file.isFile) {
            return Failure(RunnerError(Analyzing, "Config file not found: ${file.absolutePath}"))
        }

        val fmt: ConfigFormat? = when (file.extension.lowercase()) {
            "yml", "yaml" -> ConfigFormat.YAML
            "json" -> ConfigFormat.JSON
            else -> null // que el loader decida con byExtension()
        }

        return when (val res = AnalyzerConfigLoader.fromFile(file, fmt)) {
            is Success -> Success(res.value)
            is Failure -> {
                val msg = res.error.message ?: "invalid analyzer config"
                Failure(RunnerError(Analyzing, "Invalid analyzer config: $msg"))
            }
        }
    }
}
