package org.printscript.analyzer

import org.printscript.ast.ProgramNode
import org.printscript.common.LabeledError
import org.printscript.common.Result

interface Analyzer {
    fun analyze(program: ProgramNode, config: AnalyzerConfig): Result<DiagnosticReport, LabeledError>
}
