package org.printscript.analyzer.loader

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object AnalyzerConfigLoader {

    fun fromFile(file: File, format: ConfigFormat? = null): Result<AnalyzerConfig, LabeledError> =
        try {
            val reader = when (format ?: byExtension(file.extension)) {
                ConfigFormat.JSON -> JsonConfigReader()
                ConfigFormat.YAML -> YamlConfigReader()
            }
            FileInputStream(file).use { input -> reader.load(input) }
        } catch (e: IOException) {
            Failure(
                LabeledError.Companion.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Error al leer archivo '${file.path}': ${e.message}",
                ),
            )
        }

    private fun byExtension(ext: String) = when (ext.lowercase()) {
        "yml", "yaml" -> ConfigFormat.YAML
        else -> ConfigFormat.JSON
    }
}
