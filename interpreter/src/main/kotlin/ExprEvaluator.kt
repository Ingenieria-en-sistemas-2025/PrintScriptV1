import org.example.InterpreterError
import org.example.Value

fun interface ExprEvaluator {
    fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError>
}