package org.printscript.interpreter

import org.printscript.ast.ConstDeclaration
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class ConstDeclarationAction : StatementAction<ConstDeclaration> {
    override fun run(
        stmt: ConstDeclaration,
        env: Env,
        out: Output,
        eval: ExprEvaluator,
    ): Result<ExecResult, InterpreterError> {
        var outAcc = out

        val envWithPrompt = env.withInput(
            PromptingInputProvider(env.inputProvider()) { s -> outAcc = outAcc.append(s) },
        )

        return eval.evaluate(stmt.initializer, envWithPrompt).flatMap { value ->
            env.declareConst(stmt.name, stmt.type, value, stmt.span)
                .map { newEnv -> ExecResult(newEnv, outAcc) }
        }
    }
}
