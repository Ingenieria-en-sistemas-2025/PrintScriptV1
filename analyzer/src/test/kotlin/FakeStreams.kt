import org.printscript.ast.Statement
import org.printscript.ast.StatementStream
import org.printscript.ast.Step
import org.printscript.common.LabeledError

fun streamOf(vararg stmts: Statement): StatementStream {
    data class S(val rest: List<Statement>) : StatementStream {
        override fun nextStep(): Step = when {
            rest.isEmpty() -> Step.Eof
            else -> Step.Item(rest.first(), S(rest.drop(1)))
        }
    }
    return S(stmts.toList())
}

fun streamWithError(err: LabeledError, after: List<Statement>): StatementStream =
    object : StatementStream {
        private var fired = false
        override fun nextStep(): Step =
            if (!fired) {
                fired = true
                Step.Error(err, streamOf(*after.toTypedArray()))
            } else {
                Step.Eof
            }
    }
