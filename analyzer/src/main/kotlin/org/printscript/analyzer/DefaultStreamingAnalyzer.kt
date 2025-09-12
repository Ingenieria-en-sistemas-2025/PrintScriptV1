// package org.printscript.analyzer
//
// import org.printscript.ast.StatementStream
// import org.printscript.common.Failure
// import org.printscript.common.LabeledError
// import org.printscript.common.Result
// import org.printscript.common.Success
//
// class DefaultStreamingAnalyzer(private val rules: List<StreamingRule>) : StreamingAnalyzer {
//    override fun analyze(
//        stream: StatementStream,
//        config: AnalyzerConfig,
//        out: DiagnosticEmitter,
//    ): Result<Unit, LabeledError> {
//        val context = AnalyzerContext(config)
//
//        tailrec fun loop(current: StatementStream): Result<Unit, LabeledError> {
//            if (current.isEof()) {
//                rules.forEach { it.onFinish(context, out) }
//                return Success(Unit)
//            }
//            return when (val result = current.next()) {
//                is Success -> {
//                    val (stmt, nextStream) = result.getOrNull()!!
//                    rules.forEach { it.onStatement(stmt, context, out) }
//                    loop(nextStream) // recursivo en TCO; o podés hacerlo iterativo
//                }
//                is Failure -> {
//                    val err = result.errorOrNull()!!
//                    // Reportás el error y pedís el próximo; el parser se encarga de recuperar/sincronizar
//                    out.report(Diagnostic("PS-SYNTAX", err.message, err.span, Severity.ERROR))
//                    // Intentamos seguir: si el stream quedó en estado recuperable, el próximo next() avanzará
//                    loop(current)
//                }
//            }
//        }
//        return loop(stream)
//    }
// }
