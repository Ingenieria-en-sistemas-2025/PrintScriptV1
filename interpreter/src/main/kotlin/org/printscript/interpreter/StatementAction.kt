package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result

interface StatementAction<S : Statement> {
    fun run(stmt: S, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError>
}
