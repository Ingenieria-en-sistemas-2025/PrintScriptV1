package org.printscript.runner.helpers

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.loader.ConfigFormat
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.runner.RunnerError
import org.printscript.runner.Stage
import kotlin.io.path.Path
import kotlin.io.path.exists

object AnalyzerConfigResolver {
    fun fromPathStrict(path: String?): Result<AnalyzerConfig, RunnerError> {
        if (path.isNullOrBlank()) return Success(AnalyzerConfig())

        val p = Path(path)
        if (!p.exists()) {
            return Failure(RunnerError(Stage.Analyzing, "Config file not found: ${p.toAbsolutePath()}"))
        }

        val file = p.toFile()
        if (!file.isFile) {
            return Failure(RunnerError(Stage.Analyzing, "Config path is not a file: ${file.absolutePath}"))
        }

        val fmt: ConfigFormat? = when (file.extension.lowercase()) {
            "yml", "yaml" -> ConfigFormat.YAML
            "json" -> ConfigFormat.JSON
            else -> null // que el loader decida
        }

        return when (val res = AnalyzerConfigLoader.fromFile(file, fmt)) {
            is Success -> Success(res.value)
            is Failure -> {
                val msg = res.error.message ?: "invalid analyzer config"
                Failure(RunnerError(Stage.Analyzing, "Invalid analyzer config: $msg"))
            }
        }
    }
}
