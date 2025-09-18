package org.printscript.cli

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path

class CommonOptions : OptionGroup() {
    val file: Path by option("-f", "--file", help = "Archivo fuente")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
        .required()

    val version by option("-v", "--version", help = "1.0 | 1.1")

    val config: Path? by option("-c", "--config", help = "Archivo de configuración (analyzer/formatter)")
        .path(mustExist = true, canBeFile = true, canBeDir = false)
}
