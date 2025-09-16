package org.printscript.runner.helpers

import org.printscript.analyzer.Diagnostic
import org.printscript.common.Position
import org.printscript.common.Span

object DiagnosticStringFormatter {
    fun format(d: Diagnostic): String {
        val sev = d.severity.name
        val rule = d.ruleId
        val msg = d.message
        val loc = spanToString(d.span)
        return "[$sev][$rule] $msg @ $loc"
    }

    private fun spanToString(span: Span?): String {
        if (span == null) return "(?:?:?-?:?)"
        val a = span.start
        val e = span.end
        return "${pos(a)}-${pos(e)}"
    }
    private fun pos(p: Position?): String =
        if (p == null) "L?:C?" else "L${p.line}:C${p.column}"
}
