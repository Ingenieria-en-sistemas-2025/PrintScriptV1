package org.printscript.interpreter

import org.printscript.ast.IfStmt
import org.printscript.ast.Statement
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.interpreter.errors.InternalRuntimeError
import org.printscript.interpreter.errors.InterpreterError

class IfStmtAction(private val executor: StmtExecutor) : StatementAction<IfStmt> {
    override fun run(stmt: IfStmt, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> {
        var outAcc = out

        val envWithPrompt = env.withInput(
            PromptingInputProvider(env.inputProvider()) { s -> outAcc = outAcc.append(s) },
        )

        return eval.evaluate(stmt.condition, envWithPrompt).flatMap { condValue ->
            val cond = when (condValue) {
                is Value.Bool -> condValue.b
                else -> return@flatMap Failure(InternalRuntimeError(stmt.span, "if condition must be a boolean"))
            }

            val block: List<Statement> = if (cond) stmt.thenBranch else stmt.elseBranch ?: emptyList()

            val start: Result<ExecResult, InterpreterError> = Success(ExecResult(env, outAcc))

            block.fold(start) { acc, st ->
                acc.flatMap { prev ->
                    executor.execute(st, prev.env, prev.out)
                }
            }
        }
    }
}
