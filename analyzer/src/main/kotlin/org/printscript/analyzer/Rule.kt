package org.printscript.analyzer

import org.printscript.ast.ProgramNode

interface Rule {
    val id: String
    fun check(program: ProgramNode, context: AnalyzerContext): List<Diagnostic>
}
