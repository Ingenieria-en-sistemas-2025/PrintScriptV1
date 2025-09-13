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
        // evaluar condicion
        return eval.evaluate(stmt.condition, env).flatMap { condValue ->
            val cond = when (condValue) {
                is Value.Bool -> condValue.b
                else -> return@flatMap Failure(
                    InternalRuntimeError(stmt.span, "if condition must be a boolean"),
                )
            }

            val block: List<Statement> = if (cond) stmt.thenBranch else stmt.elseBranch ?: emptyList()

            val start = Success(ExecResult(env, out)) as Result<ExecResult, InterpreterError>

            block.fold(start) { acc, st ->
                acc.flatMap { prev ->
                    executor.execute(st, prev.env, prev.out)
                }
            }
        }
    }
}
