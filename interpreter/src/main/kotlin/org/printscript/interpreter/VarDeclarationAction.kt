package org.printscript.interpreter

import org.printscript.ast.VarDeclaration
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.interpreter.errors.InterpreterError

class VarDeclarationAction : StatementAction<VarDeclaration> {
    override fun run(stmt: VarDeclaration, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> {
        // evalaur si inicializer eixste
        val initResult: Result<Value?, InterpreterError> =
            stmt.initializer
                ?.let { e -> eval.evaluate(e, env).map { v -> v } }
                ?: Success(null)

        return initResult.flatMap { maybeV ->
            env.declare(stmt.name, stmt.type, maybeV, stmt.span)
                .map { newEnv -> ExecResult(newEnv, out) }
        }
    }
}
