package org.printscript.analyzer.config

import org.printscript.analyzer.rules.IdentifierStyle

data class IdentifiersConfig(
    val style: IdentifierStyle = IdentifierStyle.CAMEL_CASE,
    val customRegex: String? = null,
    val checkReferences: Boolean = false,
    val failOnViolation: Boolean = false, // por defecto solo warning.
)
