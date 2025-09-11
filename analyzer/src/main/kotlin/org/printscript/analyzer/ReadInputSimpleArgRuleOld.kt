package org.printscript.analyzer

import org.printscript.ast.Expression
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.ProgramNode
import org.printscript.ast.ReadInput
import org.printscript.ast.Variable

class ReadInputSimpleArgRuleOld : Rule {
    override val id = "PS-READINPUT-SIMPLE"

    private fun isIdOrLiteral(e: Expression) =
        e is Variable || e is LiteralString || e is LiteralNumber || e is LiteralBoolean

    private fun isIdOrStringOnly(e: Expression) =
        e is Variable || e is LiteralString

    override fun check(program: ProgramNode, context: AnalyzerContext): Sequence<Diagnostic> {
        if (isDisabled(context)) return emptySequence()
        val config = context.config.readInputRule

        return AstWalk.ofType<ReadInput>(program).mapNotNull { call ->
            val ok = if (config.onlyStringLiteralOrIdentifier) {
                isIdOrStringOnly(call.prompt)
            } else {
                isIdOrLiteral(call.prompt)
            }

            if (ok) {
                null
            } else {
                Diagnostic(
                    id,
                    "readInput solo admite identificador o literal como argumento (no expresiones compuestas)",
                    call.prompt.span,
                    Severity.ERROR,
                )
            }
        }
    }

    private fun isDisabled(context: AnalyzerContext): Boolean {
        return !context.config.printlnRule.enabled
    }
}
