package org.printscript.analyzer

interface DiagnosticEmitter {
    fun report(diagnostic: Diagnostic)
}
