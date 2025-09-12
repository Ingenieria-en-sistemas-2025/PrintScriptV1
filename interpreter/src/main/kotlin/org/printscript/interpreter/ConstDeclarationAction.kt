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
    ): Result<ExecResult, InterpreterError> =
        eval.evaluate(stmt.initializer, env).flatMap { value ->
            env.declare(stmt.name, stmt.type, value, stmt.span).map { newEnv ->
                ExecResult(newEnv, out)
            }
        }
}
