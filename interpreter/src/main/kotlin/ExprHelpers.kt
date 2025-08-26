class ExprHelpers private constructor() {
    companion object {
        fun formatNumber(x: Double): String =
            if (x % 1.0 == 0.0) x.toInt().toString() else x.toString()

        fun typeName(v: Value): String = when (v) {
            is Value.Num -> "number"
            is Value.Str -> "string"
        }

        fun addOrConcat(span: Span, left: Value, right: Value): Result<Value, InterpreterError> = when {
            left is Value.Str && right is Value.Str ->
                Success(Value.Str(left.s + right.s))
            left is Value.Str && right is Value.Num ->
                Success(Value.Str(left.s + formatNumber(right.n)))
            left is Value.Num && right is Value.Str ->
                Success(Value.Str(formatNumber(left.n) + right.s))
            left is Value.Num && right is Value.Num ->
                Success(Value.Num(left.n + right.n))
            else ->
                Failure(UnsupportedBinaryOp(span, Operator.PLUS, typeName(left), typeName(right)))
        }

        fun numericOp(
            span: Span,
            left: Value,
            right: Value,
            op: Operator,
            f: (Double, Double) -> Double,
        ): Result<Value, InterpreterError> {
            if (left !is Value.Num || right !is Value.Num) {
                return Failure(UnsupportedBinaryOp(span, op, typeName(left), typeName(right)))
            }
            if (op == Operator.DIVIDE && right.n == 0.0) {
                return Failure(DivisionByZero(span))
            }
            return Success(Value.Num(f(left.n, right.n)))
        }

        val defaultBinaryOps: Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>> =
            mapOf(
                Operator.PLUS to { span, l, r -> addOrConcat(span, l, r) },
                Operator.MINUS to { span, l, r -> numericOp(span, l, r, Operator.MINUS) { a, b -> a - b } },
                Operator.MULTIPLY to { span, l, r -> numericOp(span, l, r, Operator.MULTIPLY) { a, b -> a * b } },
                Operator.DIVIDE to { span, l, r -> numericOp(span, l, r, Operator.DIVIDE) { a, b -> a / b } },
            )

        fun applyBinaryOp(
            ops: Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>>, // k,v : el v es la impl de como aplicar ese operador a 2 value
            op: Operator, // op encontrado en el ast
            span: Span,
            left: Value,
            right: Value,
        ): Result<Value, InterpreterError> {
            val fn = ops[op] // busca en el map la func asociafanda a ese operador
                ?: return Failure(UnsupportedBinaryOp(span, op, typeName(left), typeName(right)))
            return fn(span, left, right)
        }

        fun buildBinaryOps(
            vararg overrides: Pair<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>>,
        ): Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>> {
            return defaultBinaryOps + overrides
        }
    }
}
