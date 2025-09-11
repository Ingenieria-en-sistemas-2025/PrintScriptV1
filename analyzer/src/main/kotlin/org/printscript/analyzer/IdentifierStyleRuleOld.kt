package org.printscript.analyzer

import org.printscript.ast.Assignment
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.ProgramNode
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Span

class IdentifierStyleRuleOld(
    private val conventionProvider: (IdentifiersConfig) -> NameConvention = { IdentifierNaming.from(it) },
) : Rule {

    override val id = "PS-ID-STYLE"

    override fun check(program: ProgramNode, context: AnalyzerContext): Sequence<Diagnostic> = sequence {
        val config = context.config.identifiers
        val conv = conventionProvider(config)
        val severity = if (config.failOnViolation) Severity.ERROR else Severity.WARNING

        val seen = HashSet<Pair<String, Span>>()
        fun deDupe(diag: Diagnostic): Diagnostic? {
            val key = (diag.message to diag.span)
            return if (seen.add(key)) diag else null
        }

        // 1) Nombres en declaraciones y LHS de asignaciones
        yieldAll(checkDeclsAndAssignments(program, conv, severity).mapNotNull(::deDupe))

        // 2) Referencias en expresiones (opcional)
        if (config.checkReferences) {
            yieldAll(checkReferences(program, conv, severity).mapNotNull(::deDupe))
        }
    }

    private fun checkDeclsAndAssignments(
        program: ProgramNode,
        conv: NameConvention,
        sev: Severity,
    ): Sequence<Diagnostic> =
        AstWalk.statements(program).flatMap { st ->
            when (st) {
                is VarDeclaration ->
                    if (!conv.matches(st.name)) sequenceOf(diag(st.span, st.name, sev)) else emptySequence()
                is ConstDeclaration ->
                    if (!conv.matches(st.name)) sequenceOf(diag(st.span, st.name, sev)) else emptySequence()
                is Assignment ->
                    if (!conv.matches(st.name)) sequenceOf(diag(st.span, st.name, sev)) else emptySequence()
                else -> emptySequence()
            }
        }

    private fun checkReferences(
        program: ProgramNode,
        conv: NameConvention,
        sev: Severity,
    ): Sequence<Diagnostic> =
        AstWalk.ofType<Variable>(program).mapNotNull { v ->
            if (!conv.matches(v.name)) diag(v.span, v.name, sev) else null
        }

    private fun diag(span: Span, name: String, sev: Severity) =
        Diagnostic(id, "Identificador '$name' no respeta ${"convención"}", span, sev)
}
