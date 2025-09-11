package org.printscript.interpreter
import org.printscript.ast.Assignment
import org.printscript.ast.Println
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.common.Failure
import org.printscript.common.Result
import kotlin.reflect.KClass

class StmtActionExecutor(
    private val eval: ExprEvaluator,
    private val actions: Map<KClass<out Statement>, StatementAction<out Statement>> = mapOf( // sabe q accion ejecutar en ese tipo de sentencia
        VarDeclaration::class to VarDeclarationAction(),
        Assignment::class to AssignmentAction(),
        Println::class to PrintlnAction(),
    ),
) : StmtExecutor {

    @Suppress("UNCHECKED_CAST")
    override fun execute(
        stmt: Statement,
        env: Env,
        out: Output,
    ): Result<ExecResult, InterpreterError> {
        val action = actions[stmt::class]
            ?: return Failure(InternalRuntimeError(stmt.span, "Sentencia no soportada: $stmt"))
        // casteo seguro por clave del mapa
        val act = action as StatementAction<Statement>
        return act.run(stmt, env, out, eval)
    }
}

// KClass<out Statement> -> out Statement permite claves que sean subtipos de Statement org.printscript.interpreter.StatementAction<out Statement>
// @Suppress("UNCHECKED_CAST") ->garantizo que el mapa está bien construido, le doy confianza al compilador y suprimo el warning
// el cast es seguro porque la clave del mapa es stmt::class. Eso asegura que la action recuperada corresponde al mismo subtipo que stmt
