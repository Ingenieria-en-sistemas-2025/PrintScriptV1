package org.printscript.interpreter

import org.printscript.ast.Println
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class PrintlnAction : StatementAction<Println> {
    override fun run(
        stmt: Println,
        env: Env,
        out: Output,
        eval: ExprEvaluator,
    ): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.value, env).map { v ->
            val line = when (v) {
                is Value.Str -> v.s
                is Value.Num -> ExprHelpers.formatNumber(v.n)
                is Value.Bool -> v.b.toString()
            }

            // Siempre emitimos (para PrintEmitter / PrintCounter / prompts, etc.)
            env.emit(line)

            // DECISIÓN CLAVE:
            // - Si hay printer y preferimos "sink", NO acumulamos.
            // - Si no hay printer, o hay printer pero NO preferimos "sink", SÍ acumulamos.
            val nextOut =
                if (env.hasPrinter() && env.preferSinkWithPrinter()) {
                    out
                } else {
                    out.append(line)
                }

            ExecResult(env, nextOut)
        }
}
