package org.printscript.interpreter

import org.printscript.ast.Println
import org.printscript.common.Result

class PrintlnAction : StatementAction<Println> {
    override fun run(stmt: Println, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.value, env).map { v ->
            val line = when (v) { // convierto el org.printscript.interpreter.Value a String para imprimir
                is Value.Str -> v.s
                is Value.Num -> ExprHelpers.formatNumber(v.n)
                is Value.Bool -> v.b.toString()
            }
            ExecResult(env, out.append(line)) // mismo env nuevo output
        }
}
