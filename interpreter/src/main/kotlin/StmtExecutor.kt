import org.example.InterpreterError
import org.example.Output

//dado un stmt acrualiza el estado del prog
interface StmtExecutor {
    fun execute(stmt: Statement, env: Env, out: Output): Result<ExecResult, InterpreterError>
}