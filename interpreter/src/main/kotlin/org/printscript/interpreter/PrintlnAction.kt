package org.printscript.interpreter

import org.printscript.ast.Println
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class PrintlnAction : StatementAction<Println> {
    override fun run(stmt: Println, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.value, env).map { v ->
            val line = when (v) {
                is Value.Str -> v.s
                is Value.Num -> ExprHelpers.formatNumber(v.n)
                is Value.Bool -> v.b.toString()
            }

            env.emit(line)

            val mustCollect = !env.hasPrinter() || env.shouldCollectWithPrinter()
            val nextOut = if (mustCollect) out.append(line) else out

            ExecResult(env, nextOut)
        }
}
