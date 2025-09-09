package org.printscript.interpreter

import org.printscript.ast.Binary
import org.printscript.ast.Expression
import org.printscript.ast.Grouping
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Variable
import org.printscript.common.Failure
import org.printscript.common.Operator
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success

class DefaultExprEvaluator(
    private val ops: Map<Operator, (Span, Value, Value) -> Result<Value, InterpreterError>> = ExprHelpers.defaultBinaryOps,
) : ExprEvaluator {
    override fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError> =
        when (expr) {
            is LiteralNumber -> {
                val n = expr.raw.toDoubleOrNull()
                    ?: return Failure(InvalidNumericLiteral(expr.span, expr.raw))
                Success(Value.Num(n))
            }
            is LiteralString -> Success(Value.Str(expr.value))
            is Variable -> {
                val b = env.lookup(expr.name) // busca el env
                    ?: return Failure(UndeclaredVariable(expr.span, expr.name))
                Success(b.value) // si existe succes sino fail
            }
            is Grouping -> evaluate(expr.expression, env) // evalua recursivo adentro

            is Binary -> {
                val leftR = evaluate(expr.left, env)
                val rightR = evaluate(expr.right, env)
                leftR.flatMap { l ->
                    rightR.flatMap { r ->
                        // Usamos la tabla de operadores: extensible y testeable
                        ExprHelpers.applyBinaryOp(ops, expr.operator, expr.span, l, r)
                    }
                }
            }

            else -> Failure(InternalRuntimeError(expr.span, "Not supported expr: $expr"))
        }
}
