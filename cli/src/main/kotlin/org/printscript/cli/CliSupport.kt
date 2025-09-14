package org.printscript.cli

import org.printscript.common.Position
import org.printscript.common.Span
import org.printscript.common.Version
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object CliSupport {
    fun resolveVersion(raw: String?): Version =
        when (raw?.trim()) {
            null, "", "1.0", "v1.0" -> Version.V0 // si tu enum es V0 para 1.0
            "1.1", "v1.1" -> Version.V1
            else -> error("Versión desconocida: $raw (use 1.0 o 1.1)")
        }

    fun readSourceOrFail(path: String): String {
        val p = Path.of(path)
        require(p.exists()) { "No existe el archivo: $path" }
        return Files.readString(p)
    }

    fun readConfigPathOrFail(path: String?): Path {
        require(!path.isNullOrBlank()) { "Falta --config" }
        val p = Path.of(path)
        require(p.exists()) { "No existe el archivo de configuración: $path" }
        return p
    }

    fun formatSpan(span: Span?): String =
        if (span == null) {
            "(sin ubicación)"
        } else {
            "@ ${pos(span.start)} → ${pos(span.end)}"
        }

    private fun pos(p: Position?): String =
        if (p == null) {
            "?"
        } else {
            "L${p.line}:C${p.column}"
        }
}
