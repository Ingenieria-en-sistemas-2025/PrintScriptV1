package org.printscript.runner.helpers

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.loader.ConfigFormat
import org.printscript.common.Success
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object AnalyzerConfigLoaderFromPath {

    fun fromPath(path: String?): AnalyzerConfig {
        if (path.isNullOrBlank()) return AnalyzerConfig()

        val p: Path = Path(path)
        if (!p.exists()) return AnalyzerConfig()

        val file = p.toFile()

        val rJson = AnalyzerConfigLoader.fromFile(file, ConfigFormat.JSON)
        if (rJson is Success) return rJson.getOrNull()

        val rYaml = AnalyzerConfigLoader.fromFile(file, ConfigFormat.YAML)
        if (rYaml is Success) return rYaml.getOrNull()

        return AnalyzerConfig()
    }
}
