package org.printscript.analyzer.rules

import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.config.AnalyzerContext
import org.printscript.ast.Statement

interface StreamingRule {
    val id: String
    fun onStatement(statement: Statement, context: AnalyzerContext, out: DiagnosticEmitter)
    fun onFinish(context: AnalyzerContext, out: DiagnosticEmitter) {}
}
