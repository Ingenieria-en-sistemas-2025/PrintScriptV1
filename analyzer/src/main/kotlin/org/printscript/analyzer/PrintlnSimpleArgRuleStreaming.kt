package org.printscript.analyzer

import org.printscript.ast.Expression
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.Statement
import org.printscript.ast.Variable

class PrintlnSimpleArgRuleStreaming : StreamingRule {
    override val id: String = "PS-PRINTLN-SIMPLE"

    private fun isSimple(v: Expression) =
        v is Variable || v is LiteralString || v is LiteralNumber || v is LiteralBoolean

    override fun onStatement(statement: Statement, context: AnalyzerContext, out: DiagnosticEmitter) {
        if (!context.config.printlnRule.enabled) return
        if (statement is Println) {
            val v = statement.value
            if (!isSimple(v)) {
                out.report(
                    Diagnostic(id, "println solo admite identificador o literal (no expresiones compuestas)", v.span, Severity.ERROR),
                )
            }
        }
    }
}
