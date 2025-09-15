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

            // siempre emito (para PrintEmitter / PrintCounter / prompts, etc.)
            env.emit(line)

            // si hay printer externo, no acumuko en Output para evitar OOM;
            // si no hay, mantengo el collecting para los tests que leen asList().
            val nextOut = if (env.hasPrinter()) out else out.append(line)

            ExecResult(env, nextOut)
        }
}
