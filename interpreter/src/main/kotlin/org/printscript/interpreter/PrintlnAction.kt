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
    ): Result<ExecResult, InterpreterError> {
        var outAcc = out

        val envWithPrompt = env.withInput(
            PromptingInputProvider(env.inputProvider()) { s -> outAcc = outAcc.append(s) },
        )

        return eval.evaluate(stmt.value, envWithPrompt).map { v ->
            val line = when (v) {
                is Value.Str -> v.s
                is Value.Num -> ExprHelpers.formatNumber(v.n)
                is Value.Bool -> v.b.toString()
            }
            ExecResult(env, outAcc.append(line))
        }
    }
}
