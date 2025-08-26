fun interface ExprEvaluator {
    fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError>
}
