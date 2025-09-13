package org.printscript.runner

import org.printscript.analyzer.Diagnostic

data class ValidationReport(
    val diagnostics: List<Diagnostic>,
    val hasErrors: Boolean,
)
