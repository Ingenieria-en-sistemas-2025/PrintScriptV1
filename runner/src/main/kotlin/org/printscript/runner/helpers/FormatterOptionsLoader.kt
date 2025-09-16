package org.printscript.runner.helpers

import org.printscript.formatter.config.ExternalFormatterConfigLoader
import org.printscript.formatter.config.FormatterConfig
import org.printscript.formatter.config.FormatterOptions
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

object FormatterOptionsLoader {

    /** Carga desde ruta; si es null/blank o falla, devuelve defaults. */
    fun fromPath(path: String?): FormatterOptions {
        if (path.isNullOrBlank()) return FormatterConfig()
        return try {
            val p: Path = Path(path)
            val bytes = Files.readAllBytes(p)
            if (bytes.isEmpty()) {
                FormatterConfig()
            } else {
                ExternalFormatterConfigLoader.load(bytes)
            }
        } catch (_: Throwable) {
            FormatterConfig()
        }
    }

    /** Carga desde stream; si es null o falla, devuelve defaults. */
    fun fromStream(config: InputStream?): FormatterOptions =
        try {
            if (config == null) {
                FormatterConfig()
            } else {
                val bytes = config.readAllBytes()
                if (bytes.isEmpty()) FormatterConfig() else ExternalFormatterConfigLoader.load(bytes)
            }
        } catch (_: Throwable) {
            FormatterConfig()
        }

    /** Carga desde bytes; si es vacío o falla, devuelve defaults. */
    fun fromBytes(bytes: ByteArray?): FormatterOptions =
        try {
            if (bytes == null || bytes.isEmpty()) {
                FormatterConfig()
            } else {
                ExternalFormatterConfigLoader.load(bytes)
            }
        } catch (_: Throwable) {
            FormatterConfig()
        }
}
