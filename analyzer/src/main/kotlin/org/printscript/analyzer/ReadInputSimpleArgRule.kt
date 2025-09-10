package org.printscript.analyzer

import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.ast.Grouping
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.ProgramNode
import org.printscript.ast.ReadEnv
import org.printscript.ast.ReadInput
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable

class ReadInputSimpleArgRule : Rule {
    override val id: String = "PS-READINPUT-SIMPLE"

    override fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic> {
        val cfg = context.config.readInputRule
        if (!cfg.enabled) return emptyList()

        val diags = mutableListOf<Diagnostic>()
        program.statements.forEach { visitStmt(it, cfg, diags) }
        return diags
    }

    private fun promptIsOk(cfg: ReadInputRuleConfig, e: Expression): Boolean =
        if (cfg.onlyStringLiteralOrIdentifier) isIdentifierOrStringOnly(e) else isIdentifierOrAnyLiteral(e)

    private fun isIdentifierOrAnyLiteral(e: Expression): Boolean = when (e) {
        is Variable -> true
        is LiteralString, is LiteralNumber -> true
        // is LiteralBoolean -> true por si lo agregamos
        else -> false
    }

    private fun isIdentifierOrStringOnly(e: Expression): Boolean = when (e) {
        is Variable -> true
        is LiteralString -> true
        else -> false
    }

    private fun visitExpr(e: Expression, cfg: ReadInputRuleConfig, diags: MutableList<Diagnostic>) {
        when (e) {
            is ReadInput -> {
                if (!promptIsOk(cfg, e.prompt)) {
                    diags += Diagnostic(
                        ruleId = id,
                        message = "readInput solo admite identificador o literal como argumento (no expresiones compuestas)",
                        span = e.prompt.span,
                        severity = Severity.ERROR,
                    )
                }
                // seguir recorriendo no hace daño (Variable/Literal no recursan)
                visitExpr(e.prompt, cfg, diags)
            }

            is ReadEnv -> {
                // Si el nombre fuera Expression en tu AST y quisieramos validarlo
            }

            is Binary -> {
                visitExpr(e.left, cfg, diags)
                visitExpr(e.right, cfg, diags)
            }
            is Grouping -> visitExpr(e.expression, cfg, diags)

            // No-op para hojas
            is LiteralString, is LiteralNumber, is Variable -> Unit

            else -> Unit // por si agregamos nuevas expresiones
        }
    }

    private fun visitStmt(st: Statement, cfg: ReadInputRuleConfig, diags: MutableList<Diagnostic>) {
        when (st) {
            is VarDeclaration -> st.initializer?.let { visitExpr(it, cfg, diags) }
            is Assignment -> visitExpr(st.value, cfg, diags)
            is Println -> visitExpr(st.value, cfg, diags)
            is IfStmt -> {
                visitExpr(st.condition, cfg, diags)
                st.thenBranch.forEach { visitStmt(it, cfg, diags) }
                st.elseBranch?.forEach { visitStmt(it, cfg, diags) }
            }
            else -> Unit
        }
    }
}
