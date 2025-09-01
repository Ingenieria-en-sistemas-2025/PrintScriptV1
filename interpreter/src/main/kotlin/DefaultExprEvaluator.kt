

class DefaultExprEvaluator(
    private val ops: Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>> = ExprHelpers.defaultBinaryOps,
) : ExprEvaluator {
    override fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError> =
        // ADTTTTTTTTTTTT
        when (expr) {
            is LiteralNumber -> {
                return literalNumber(expr)
            }
            is LiteralString -> Success(Value.Str(expr.value))
            is Variable -> {
                return findVarContext(env, expr)
            }
            is Grouping -> evaluate(expr.expression, env) // evalua recursivo adentro

            is Binary -> {
                resolveBinary(expr, env)
            }

            else -> Failure(InternalRuntimeError(expr.span, "Not supported expr: $expr"))
        }

    private fun findVarContext(env: Env, expr: Variable): Result<Value, UndeclaredVariable> {
        val b = env.lookup(expr.name) // busca el env
            ?: return Failure(UndeclaredVariable(expr.span, expr.name))
        return Success(b.value) // si existe succes sino fail
    }

    private fun literalNumber(expr: LiteralNumber): Result<Value.Num, InvalidNumericLiteral> {
        val n = expr.raw.toDoubleOrNull()
            ?: return Failure(InvalidNumericLiteral(expr.span, expr.raw))
        return Success(Value.Num(n))
    }

    private fun resolveBinary(expr: Binary, env: Env): Result<Value, InterpreterError> {
        val leftR = evaluate(expr.left, env)
        val rightR = evaluate(expr.right, env)
        return leftR.flatMap { l ->
            rightR.flatMap { r ->
                // Usamos la tabla de operadores: extensible y testeable
                ExprHelpers.applyBinaryOp(ops, expr.operator, expr.span, l, r)
            }
        }
    }
}
