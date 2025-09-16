package org.printscript.runner

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.Severity

internal class CappedDiagnosticCollector(
    private val keepLast: Int = 2000,
    private val hardLimit: Int = 100_000, // corta flood
) : DiagnosticEmitter {
    private val deque = ArrayDeque<Diagnostic>(keepLast)
    private var total = 0
    private var errors = 0
    private var truncated = false

    override fun report(diagnostic: Diagnostic) {
        total++
        if (diagnostic.severity == Severity.ERROR) errors++
        if (deque.size == keepLast) deque.removeFirst()
        deque.addLast(diagnostic)
        if (total >= hardLimit) truncated = true
    }

    fun hasErrors(): Boolean = errors > 0
    fun truncated(): Boolean = truncated
    fun snapshot(): List<Diagnostic> = deque.toList()
    fun stats(): Pair<Int, Int> = total to errors
}
