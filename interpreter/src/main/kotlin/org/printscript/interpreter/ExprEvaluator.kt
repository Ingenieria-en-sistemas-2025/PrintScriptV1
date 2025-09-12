package org.printscript.interpreter

import org.printscript.ast.Expression
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

fun interface ExprEvaluator {
    fun evaluate(expr: Expression, env: Env): Result<Value, InterpreterError>
}
