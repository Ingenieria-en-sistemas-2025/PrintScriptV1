package org.printscript.cli

import org.printscript.common.Version

object CliSupport {
    // traduce la cadena cruda de CLI a un Version tipado, y valida que sea valido
    fun resolveVersion(raw: String?): Version =
        when (raw?.trim()) {
            null, "", "1.0", "v1.0" -> Version.V0
            "1.1", "v1.1" -> Version.V1
            else -> error("Versión desconocida: $raw (use 1.0 o 1.1)")
        }
}
