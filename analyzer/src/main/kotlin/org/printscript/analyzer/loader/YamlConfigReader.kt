package org.printscript.analyzer.loader

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.toLabeledError
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import java.io.IOException
import java.io.InputStream

class YamlConfigReader(
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory())
        .registerModule(KotlinModule.Builder().build()),
) : ConfigReader {

    override fun load(input: InputStream): Result<AnalyzerConfig, LabeledError> =
        try {
            Success(mapper.readValue<AnalyzerConfig>(input))
        } catch (e: JsonProcessingException) {
            Failure(e.toLabeledError("Formato inválido (YAML)"))
        } catch (e: IOException) {
            Failure(
                LabeledError.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Error de IO leyendo YAML: ${e.message}",
                ),
            )
        } catch (e: IllegalArgumentException) {
            Failure(
                LabeledError.of(
                    Span(Position(1, 1), Position(1, 1)),
                    "Config YAML inválida: ${e.message}",
                ),
            )
        }
}
