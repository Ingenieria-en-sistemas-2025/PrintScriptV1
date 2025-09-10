package org.printscript.analyzer

import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ProgramNode
import org.printscript.ast.Variable

class PrintlnSimpleArgRule : Rule {

    override val id: String = "PS-PRINTLN-SIMPLE"
    override fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic> {
        if (!context.config.printlnRule.enabled) return emptyList()
        val out = mutableListOf<Diagnostic>()
        for (st in program.statements) {
            if (st is Println) {
                val v = st.value
                val ok = v is Variable || v is LiteralString || v is LiteralNumber
                if (!ok) {
                    out += Diagnostic(id, "println solo admite identificador o literal (no expresiones compuestas)", v.span, Severity.ERROR)
                }
            }
        }
        return out
    }
}
