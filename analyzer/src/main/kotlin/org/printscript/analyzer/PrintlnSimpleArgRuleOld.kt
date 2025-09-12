package org.printscript.analyzer

import org.printscript.ast.Expression
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ProgramNode
import org.printscript.ast.Variable

class PrintlnSimpleArgRuleOld : Rule {

    override val id: String = "PS-PRINTLN-SIMPLE"

    private fun isSimple(v: Expression) =
        v is Variable || v is LiteralString || v is LiteralNumber || v is LiteralBoolean

    override fun check(program: ProgramNode, context: AnalyzerContext): Sequence<Diagnostic> {
        if (isDisabled(context)) return emptySequence()

        return AstWalk.statements(program).filterIsInstance<Println>().mapNotNull {
                st ->
            val v = st.value
            if (isSimple(v)) {
                null
            } else {
                Diagnostic(
                    id,
                    "println solo admite identificador o literal (no expresiones compuestas)",
                    v.span,
                    Severity.ERROR,
                )
            }
        }
    }

    private fun isDisabled(context: AnalyzerContext): Boolean {
        return !context.config.printlnRule.enabled
    }
}
