package org.printscript.interpreter

import org.printscript.ast.VarDeclaration
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class VarDeclarationAction : StatementAction<VarDeclaration> {
    override fun run(
        stmt: VarDeclaration,
        env: Env,
        out: Output,
        eval: ExprEvaluator,
    ): Result<ExecResult, InterpreterError> {
        var outAcc = out

        // si no hay inicializer declaro directamente sin envolver
        val initEval: Result<Value, InterpreterError>? = stmt.initializer?.let { e ->
            val envWithPrompt = env.withInput(
                PromptingInputProvider(env.inputProvider()) { s -> outAcc = outAcc.append(s) },
            )
            eval.evaluate(e, envWithPrompt)
        }

        return if (initEval != null) {
            initEval.flatMap { v ->
                env.declare(stmt.name, stmt.type, v, stmt.span)
                    .map { newEnv -> ExecResult(newEnv, outAcc) }
            }
        } else {
            // sin initializer
            env.declare(stmt.name, stmt.type, null, stmt.span)
                .map { newEnv -> ExecResult(newEnv, outAcc) }
        }
    }
}
