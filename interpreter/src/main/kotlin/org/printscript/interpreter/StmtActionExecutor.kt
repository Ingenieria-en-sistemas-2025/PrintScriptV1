package org.printscript.interpreter
import org.printscript.ast.Assignment
import org.printscript.ast.ConstDeclaration
import org.printscript.ast.IfStmt
import org.printscript.ast.Println
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.common.Result
import org.printscript.interpreter.errors.InterpreterError

class StmtActionExecutor(
    private val eval: ExprEvaluator,
) : StmtExecutor {

    @Suppress("UNCHECKED_CAST")
    override fun execute(
        stmt: Statement,
        env: Env,
        out: Output,
    ): Result<ExecResult, InterpreterError> {
        val action = eval(stmt)
        // casteo seguro por clave del mapa
        return (action as StatementAction<Statement>).run(stmt, env, out, eval)
    }

    private fun eval(statement: Statement): StatementAction<out Statement> = when (statement) {
        is VarDeclaration -> VarDeclarationAction()
        is Assignment -> AssignmentAction()
        is Println -> PrintlnAction()
        is ConstDeclaration -> ConstDeclarationAction()
        is IfStmt -> IfStmtAction(this)
    }
}

// KClass<out Statement> -> out Statement permite claves que sean subtipos de Statement org.printscript.interpreter.StatementAction<out Statement>
// @Suppress("UNCHECKED_CAST") ->garantizo que el mapa está bien construido, le doy confianza al compilador y suprimo el warning
// el cast es seguro porque la clave del mapa es stmt::class. Eso asegura que la action recuperada corresponde al mismo subtipo que stmt
