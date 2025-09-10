package org.printscript.analyzer

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import java.io.File
import java.io.IOException

object AnalyzerConfigLoader {

    fun fromFile(file: File): Result<AnalyzerConfig, LabeledError> =
        try {
            val mapper = when (file.extension.lowercase()) {
                "yml", "yaml" -> ObjectMapper(YAMLFactory())
                else -> ObjectMapper() // JSON por default
            }.registerModule(KotlinModule.Builder().build())

            val cfg: AnalyzerConfig = mapper.readValue(file)
            Success(cfg)
        } catch (e: IOException) {
            Failure(object : LabeledError {
                override val span = Span(Position(1, 1), Position(1, 1))
                override val message = "Error al leer archivo '${file.path}': ${e.message}"
            })
        } catch (e: JsonProcessingException) {
            Failure(object : LabeledError {
                override val span = Span(Position(1, 1), Position(1, 1))
                override val message = "Formato inválido en '${file.path}': ${e.message}"
            })
        }
}
