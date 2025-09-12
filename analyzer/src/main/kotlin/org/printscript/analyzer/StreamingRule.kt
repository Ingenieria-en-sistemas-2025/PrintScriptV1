package org.printscript.analyzer

import org.printscript.ast.Statement

interface StreamingRule {
    val id: String
    fun onStatement(statement: Statement, context: AnalyzerContext, out: DiagnosticEmitter)
    fun onFinish(context: AnalyzerContext, out: DiagnosticEmitter) {}
}
