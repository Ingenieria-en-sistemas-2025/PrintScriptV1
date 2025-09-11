package org.printscript.analyzer

import org.printscript.ast.Expression
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.ReadInput
import org.printscript.ast.Statement
import org.printscript.ast.Variable

class ReadInputSimpleArgRule : StreamingRule {

    override val id = "PS-READINPUT-SIMPLE"

    private fun isIdOrLiteral(e: Expression) =
        e is Variable || e is LiteralString || e is LiteralNumber || e is LiteralBoolean
    private fun isIdOrStringOnly(e: Expression) =
        e is Variable || e is LiteralString

    override fun onStatement(
        statement: Statement,
        context: AnalyzerContext,
        out: DiagnosticEmitter,
    ) {
        val cfg = context.config.readInputRule
        if (!cfg.enabled) return

        AstWalk.expressionsOf(statement).filterIsInstance<ReadInput>().forEach { call ->
            val ok = if (cfg.onlyStringLiteralOrIdentifier) isIdOrStringOnly(call.prompt) else isIdOrLiteral(call.prompt)
            if (!ok) {
                out.report(
                    Diagnostic(id, "readInput solo admite identificador o literal como argumento (no expresiones compuestas)", call.prompt.span, Severity.ERROR),
                )
            }
        }
    }
}
