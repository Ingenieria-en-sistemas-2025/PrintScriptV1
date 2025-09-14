package org.printscript.interpreter

import org.printscript.ast.Assignment
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class AssignmentAction : StatementAction<Assignment> {
    override fun run(
        stmt: Assignment,
        env: Env,
        out: Output,
        eval: ExprEvaluator,
    ): Result<ExecResult, InterpreterError> {
        var outAcc = out // acumulador local donde voy guardando el Output actualizado con lo que vaya apareciendo

        val envWithPrompt = env.withInput(
            PromptingInputProvider(env.inputProvider()) { s -> outAcc = outAcc.append(s) },
        )

        return eval.evaluate(stmt.value, envWithPrompt)
            .flatMap { v -> env.assign(stmt.name, v, stmt.span) } // base
            .map { newEnvBase -> ExecResult(newEnvBase, outAcc) } // sin withInput redundante
    }
}
