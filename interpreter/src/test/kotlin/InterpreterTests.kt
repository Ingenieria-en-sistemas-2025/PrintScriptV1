import org.printscript.ast.Assignment
import org.printscript.ast.LiteralNumber
import org.printscript.ast.Println
import org.printscript.ast.ProgramNode
import org.printscript.ast.Statement
import org.printscript.ast.VarDeclaration
import org.printscript.common.Failure
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.interpreter.AssignmentAction
import org.printscript.interpreter.DefaultExprEvaluator
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintlnAction
import org.printscript.interpreter.ProgramInterpreter
import org.printscript.interpreter.RunResult
import org.printscript.interpreter.StmtActionExecutor
import org.printscript.interpreter.VarDeclarationAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InterpreterTests {
    private fun s() = Span(Position(1, 1), Position(1, 1))

    private fun makeInterpreter(): ProgramInterpreter {
        val eval = DefaultExprEvaluator()
        val actions = mapOf(
            VarDeclaration::class to VarDeclarationAction(),
            Assignment::class to AssignmentAction(),
            Println::class to PrintlnAction(),
        )
        val exec = StmtActionExecutor(eval, actions) // busca action en el map
        return ProgramInterpreter(exec)
    }

    @Test
    fun `println number 42`() {
        val stmts: List<Statement> = listOf(
            Println(
                // aca usa la println action
                value = LiteralNumber(raw = "42", span = s()),
                span = s(),
            ),
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> assertEquals(listOf("42"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }
}
