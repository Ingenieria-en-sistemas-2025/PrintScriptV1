import org.printscript.ast.Statement
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.LabeledError

// Stream simple que entrega todos los statements como Step.Item y luego Step.Eof.
fun streamOf(vararg stmts: Statement): StatementStream {
    return object : StatementStream {
        private var i = 0
        override fun nextStep(): Step {
            return if (i < stmts.size) {
                val stmt = stmts[i++]
                Step.Item(statement = stmt, next = this)
            } else {
                Step.Eof
            }
        }
    }
}

// Stream que emite: items de `before`, un Step.Error (inyectado), items de `after`
fun streamWithError(
    before: List<Statement>,
    error: LabeledError,
    after: List<Statement> = emptyList(),
): StatementStream {
    return object : StatementStream {
        private var phase = 0 // 0=before, 1=error, 2=after
        private var i = 0

        override fun nextStep(): Step {
            return when (phase) {
                0 -> {
                    if (i < before.size) {
                        val stmt = before[i++]
                        Step.Item(stmt, this)
                    } else {
                        // Pasamos a emitir el error
                        phase = 1
                        i = 0
                        Step.Error(error, this)
                    }
                }
                1 -> {
                    // Luego del error, si alguien siguiera (en ejecución no debería),
                    // emitiríamos los after; pero tu intérprete aborta y no los consume.
                    phase = 2
                    i = 0
                    if (after.isEmpty()) {
                        Step.Eof
                    } else {
                        val stmt = after[i++]
                        Step.Item(stmt, this)
                    }
                }
                else -> {
                    if (i < after.size) {
                        val stmt = after[i++]
                        Step.Item(stmt, this)
                    } else {
                        Step.Eof
                    }
                }
            }
        }
    }
}
