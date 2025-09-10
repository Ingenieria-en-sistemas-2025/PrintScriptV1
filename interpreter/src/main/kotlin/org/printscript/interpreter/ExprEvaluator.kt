package org.printscript.interpreter

import org.printscript.ast.Expression
import org.printscript.common.Result

fun interface ExprEvaluator {
    fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError>
}
