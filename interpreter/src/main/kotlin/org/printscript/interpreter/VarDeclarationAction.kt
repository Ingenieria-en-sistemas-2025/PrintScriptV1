package org.printscript.interpreter

import org.printscript.ast.VarDeclaration
import org.printscript.common.Result
import org.printscript.common.Success
import org.printscript.interpreter.errors.InterpreterError

class VarDeclarationAction : StatementAction<VarDeclaration> {
    override fun run(stmt: VarDeclaration, env: Env, out: Output, eval: ExprEvaluator): Result<ExecResult, InterpreterError> {
        // evalaur si inicializer eixste
        val initializerResult: Result<Value?, InterpreterError> =
            stmt.initializer?.let { e -> eval.evaluate(e, env).map { it } } ?: Success(null) // si hay inicializador lo evalua sino Success(null)

        return initializerResult.flatMap { maybeV ->
            env.declare(stmt.name, stmt.type, maybeV, stmt.span) // si es succes llamo a env.declare devuelve Result<org.printscript.interpreter.Env, org.printscript.interpreter.errors.InterpreterError>
                .map { newEnv -> ExecResult(newEnv, out) }
        }
    }
}
