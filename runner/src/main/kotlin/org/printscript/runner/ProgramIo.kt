package org.printscript.runner

import org.printscript.interpreter.InputProvider
import java.io.Reader

// paquete de entrada estandarizado para todos los Runners
data class ProgramIo(
    val source: String? = null, // src como string
    val reader: Reader? = null, // o src como stream
    val configPath: java.nio.file.Path? = null,
    val inputProviderOverride: InputProvider? = null,
) {
    init { require((source != null) xor (reader != null)) { "Provide source OR reader" } }
}
