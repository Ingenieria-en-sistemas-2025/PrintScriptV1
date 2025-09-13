package org.printscript.runner

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity

internal class RunnerDiagnosticCollector : DiagnosticEmitter {
    private val _list = mutableListOf<Diagnostic>()
    val diagnostics: List<Diagnostic> get() = _list.toList()

    override fun report(diagnostic: Diagnostic) {
        _list += diagnostic
    }
}

internal fun List<Diagnostic>.hasErrors(): Boolean =
    any { it.severity == Severity.ERROR }

internal fun List<Diagnostic>.onlyWarnings(): List<Diagnostic> =
    filter { it.severity == Severity.WARNING }
