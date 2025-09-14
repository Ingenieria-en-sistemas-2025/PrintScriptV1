package org.printscript.runner

import org.printscript.interpreter.InputProvider
import java.io.Reader

data class ProgramIo(
    val source: String? = null,
    val reader: Reader? = null,
    val configPath: java.nio.file.Path? = null,
    val inputProviderOverride: InputProvider? = null,
) {
    init { require((source != null) xor (reader != null)) { "Provide source OR reader" } }
}
