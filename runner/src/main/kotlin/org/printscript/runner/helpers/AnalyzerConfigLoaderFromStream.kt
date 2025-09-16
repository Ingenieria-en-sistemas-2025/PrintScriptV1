package org.printscript.runner.helpers

import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.loader.AnalyzerConfigLoader
import org.printscript.analyzer.loader.ConfigFormat
import org.printscript.analyzer.rules.IdentifierStyle
import org.printscript.common.Failure
import org.printscript.common.Success
import java.io.InputStream

object AnalyzerConfigLoaderFromStream {
    fun fromStream(config: InputStream?, onError: ((String) -> Unit)? = null): AnalyzerConfig {
        try {
            if (config == null || config.available() == 0) return AnalyzerConfig()

            val bytes = config.readAllBytes()
            if (bytes.isEmpty()) return AnalyzerConfig()

            val txtLower = bytes.toString(Charsets.UTF_8).trim().lowercase()
            if ("identifier_format" in txtLower) {
                val snake = "snake case" in txtLower
                val mandatory = "\"mandatory\"" in txtLower && "true" in txtLower

                val defaults = AnalyzerConfig()
                val ids = IdentifiersConfig(
                    true,
                    if (snake) IdentifierStyle.SNAKE_CASE else IdentifierStyle.CAMEL_CASE,
                    defaults.identifiers.checkReferences,
                    mandatory || defaults.identifiers.failOnViolation,
                )
                return AnalyzerConfig(ids, defaults.printlnRule, defaults.readInputRule)
            }

            val tmp = java.nio.file.Files.createTempFile("ps-analyzer-config", ".cfg").toFile()
            tmp.writeBytes(bytes)

            val rJson = AnalyzerConfigLoader.fromFile(tmp, ConfigFormat.JSON)
            if (rJson is Success) {
                tmp.delete()
                return rJson.getOrNull()
            }

            val rYaml = AnalyzerConfigLoader.fromFile(tmp, ConfigFormat.YAML)
            tmp.delete()
            if (rYaml is Success) return rYaml.getOrNull()

            val err = when {
                rYaml is Failure -> rYaml.error?.toString() ?: "unknown"
                rJson is Failure -> rJson.error?.toString() ?: "unknown"
                else -> "unknown"
            }
            onError?.invoke("config: $err")
            return AnalyzerConfig()
        } catch (ioe: java.io.IOException) {
            onError?.invoke("config: ${ioe.message}")
            return AnalyzerConfig()
        } catch (_: Throwable) {
            return AnalyzerConfig()
        }
    }
}
