package org.printscript.analyzer

import org.printscript.ast.Assignment
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.IfStmt
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Span

class IdentifierStyleRuleStreaming(
    private val conventionProvider: (IdentifiersConfig) -> NameConvention = { IdentifierNaming.from(it) },
) : StreamingRule {
    override val id = "PS-ID-STYLE"

    override fun onStatement(statement: Statement, context: AnalyzerContext, out: DiagnosticEmitter) {
        val conv = conventionProvider(context.config.identifiers)
        val sev = if (context.config.identifiers.failOnViolation) Severity.ERROR else Severity.WARNING

        fun pushIfBad(name: String, span: Span) {
            if (!conv.matches(name)) {
                out.report(Diagnostic(id, "Identificador '$name' no respeta convención", span, sev))
            }
        }

        when (statement) {
            is VarDeclaration -> pushIfBad(statement.name, statement.span)
            is ConstDeclaration -> pushIfBad(statement.name, statement.span)
            is Assignment -> pushIfBad(statement.name, statement.span)
            is IfStmt -> {
                if (context.config.identifiers.checkReferences) {
                    AstWalk.expressionsOf(statement).filterIsInstance<Variable>().forEach { v ->
                        pushIfBad(v.name, v.span)
                    }
                }
            }
            else -> {
                if (context.config.identifiers.checkReferences) {
                    AstWalk.expressionsOf(statement).filterIsInstance<Variable>().forEach { v ->
                        pushIfBad(v.name, v.span)
                    }
                }
            }
        }
    }
}
