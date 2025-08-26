import org.example.InterpreterError
import org.example.Output

interface StatementAction<S : Statement> {
    fun run(stmt: S, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError>
}