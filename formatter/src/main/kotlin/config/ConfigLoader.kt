package config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension

object ConfigLoader {
    private val json = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val yaml = ObjectMapper(YAMLFactory())
        .registerModule(com.fasterxml.jackson.module.kotlin.KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun load(path: Path?): FormatterConfig {
        if (path == null) return FormatterConfig()
        val bytes = Files.readAllBytes(path)
        return when (path.extension.lowercase()) {
            "yaml", "yml" -> yaml.readValue(bytes)
            "json" -> json.readValue(bytes)
            else -> json.readValue(bytes)
        }
    }
}
