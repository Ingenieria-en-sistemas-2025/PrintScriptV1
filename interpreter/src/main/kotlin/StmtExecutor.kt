// dado un stmt acrualiza el estado del prog
interface StmtExecutor {
    fun execute(stmt: Statement, env: Env, out: Output): Result<ExecResult, InterpreterError>
}
