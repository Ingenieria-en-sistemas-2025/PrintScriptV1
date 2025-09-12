package org.printscript.analyzer

import org.printscript.ast.StatementStream
import org.printscript.common.LabeledError
import org.printscript.common.Result

interface StreamingAnalyzer {
    fun analyze(stream: StatementStream, config: AnalyzerConfig, out: DiagnosticEmitter): Result<Unit, LabeledError>
}
