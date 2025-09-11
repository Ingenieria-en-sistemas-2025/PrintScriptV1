package org.printscript.interpreter

import org.printscript.ast.IfStmt
import org.printscript.common.Failure
import org.printscript.common.Result
import org.printscript.common.Success

class IfStmtAction(private val executor: StmtExecutor) : StatementAction<IfStmt> {
    override fun run(stmt: IfStmt, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> {
        // evaluar condicion
        return eval.evaluate(stmt.condition, env).flatMap { v ->
            val cond = when (v) {
                is Value.Bool -> v.b
                else -> return@flatMap Failure(
                    InternalRuntimeError(stmt.span, "La condición de if debe ser boolean"),
                )
            }

            val block = if (cond) stmt.thenBranch else stmt.elseBranch ?: emptyList()

            // ejecutar bloque encadenando
            var curEnv = env
            var curOut = out
            for (s in block) {
                when (val r = executor.execute(s, curEnv, curOut)) {
                    is Failure -> return@flatMap r
                    is Success -> {
                        curEnv = r.value.env
                        curOut = r.value.out
                    }
                }
            }
            Success(ExecResult(curEnv, curOut))
        }
    }
}
