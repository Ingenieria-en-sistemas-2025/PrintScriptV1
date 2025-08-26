class PrintlnAction : StatementAction<Println> {
    override fun run(stmt: Println, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.value, env).map { v ->
            val line = when (v) { // convierto el Value a String para imprimir
                is Value.Str -> v.s
                is Value.Num -> ExprHelpers.formatNumber(v.n)
            }
            ExecResult(env, out.append(line)) // mismo env nuevo output
        }
}
