

class AssignmentAction : StatementAction<Assignment> {
    override fun run(stmt: Assignment, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.value, env) // /evalua expr a la derecha del =
            .flatMap { v -> env.assign(stmt.name, v, stmt.span) } // actualiza env (Success(newEnv) si existe la variable y el tipo coincide)
            .map { newEnv -> ExecResult(newEnv, out) } // no toco output
}
