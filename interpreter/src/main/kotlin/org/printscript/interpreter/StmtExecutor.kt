package org.printscript.interpreter

import org.printscript.ast.Statement
import org.printscript.common.Result

// dado un stmt acrualiza el estado del prog
interface StmtExecutor {
    fun execute(stmt: Statement, env: Env, out: Output): Result<ExecResult, InterpreterError>
}
