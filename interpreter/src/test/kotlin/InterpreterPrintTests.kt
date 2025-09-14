import org.printscript.ast.Assignment
import org.printscript.ast.Binary
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralBoolean
import org.printscript.ast.LiteralNumber
import org.printscript.ast.LiteralString
import org.printscript.ast.Println
import org.printscript.ast.VarDeclaration
import org.printscript.ast.Variable
import org.printscript.common.Failure
import org.printscript.common.LabeledError
import org.printscript.common.Operator
import org.printscript.common.Position
import org.printscript.common.Result
import org.printscript.common.Span
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.interpreter.DefaultExprEvaluator
import org.printscript.interpreter.Interpreter
import org.printscript.interpreter.ProgramInterpreter
import org.printscript.interpreter.RunResult
import org.printscript.interpreter.StmtActionExecutor
import org.printscript.interpreter.errors.InterpreterError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InterpreterPrintTests {

    private fun s() = Span(Position(1, 1), Position(1, 1))

    private fun makeInterpreter(): Interpreter {
        val eval = DefaultExprEvaluator()
        val exec = StmtActionExecutor(eval)
        return ProgramInterpreter(exec)
    }

    @Test
    fun printlnNumber() {
        val stream = streamOf(
            Println(LiteralNumber("42", s()), s()),
        )

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(stream)

        when (res) {
            is Success -> assertEquals(listOf("42"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun declareVarAndPrintIt() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("42", s()), s()),
            Println(Variable("x", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> assertEquals(listOf("42"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun declareAssignAndPrint() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("42", s()), s()),
            Assignment("x", LiteralNumber("100", s()), s()),
            Println(Variable("x", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> assertEquals(listOf("100"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare variable x = 42, assign x = 100 and print x plus 1`() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("42", s()), s()),
            Assignment("x", LiteralNumber("100", s()), s()),
            Println(
                Binary(
                    left = Variable("x", s()),
                    operator = Operator.PLUS,
                    right = LiteralNumber("1", s()),
                    span = s(),
                ),
                s(),
            ),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> assertEquals(listOf("101"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun printString() {
        val stream = streamOf(
            Println(LiteralString("hello world", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> assertEquals(listOf("hello world"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare x=10, y=20, assign x=y, print x and y`() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("10", s()), s()),
            VarDeclaration("y", Type.NUMBER, LiteralNumber("20", s()), s()),
            Assignment("x", Variable("y", s()), s()),
            Println(Variable("x", s()), s()),
            Println(Variable("y", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> assertEquals(listOf("20", "20"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `print undeclared variable should fail`() {
        val stream = streamOf(
            Println(Variable("z", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `redeclare variable should fail`() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("1", s()), s()),
            VarDeclaration("x", Type.NUMBER, LiteralNumber("2", s()), s()),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded")
            is Failure -> println("Correctly failed: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `unsupported binary operator should fail`() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("10", s()), s()),
            Println(
                Binary(
                    left = Variable("x", s()),
                    operator = Operator.UNKNOWN,
                    right = LiteralNumber("3", s()),
                    span = s(),
                ),
                s(),
            ),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded")
            is Failure -> println("Correctly failed: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `division by zero should fail`() {
        val stream = streamOf(
            VarDeclaration("x", Type.NUMBER, LiteralNumber("10", s()), s()),
            Println(
                Binary(
                    left = Variable("x", s()),
                    operator = Operator.DIVIDE,
                    right = LiteralNumber("0", s()),
                    span = s(),
                ),
                s(),
            ),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded")
            is Failure -> println("Correctly failed: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `incompatible type should fail`() {
        val stream = streamOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralString("not a number", s()),
                span = s(),
            ),
        )

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded")
            is Failure -> println("Correctly failed: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `parser error in the middle aborts execution`() {
        // seria antes del error
        val before = listOf(
            Println(LiteralString("pre", s()), s()),
        )
        // error de parser simulado
        val parseErr: LabeledError = LabeledError.of(s(), "syntactic boom")

        // desp no deberia ejecutarse pq corte ejec
        val after = listOf(
            Println(LiteralString("post", s()), s()),
        )

        val stream = streamWithError(before = before, error = parseErr, after = after)

        val res = makeInterpreter().run(stream)
        when (res) {
            is Success -> fail("Interpreter should have failed due to parser error")
            is Failure -> println("Aborted on parser error as expected: ${res.error.humanReadable()}")
        }
    }

    @Test
    fun `if false skips then and only prints outside`() {
        val stream = streamOf(
            // const booleanValue: boolean = false;
            VarDeclaration(
                name = "booleanValue",
                type = Type.BOOLEAN,
                initializer = LiteralBoolean(false, s()),
                span = s(),
            ),
            // if (booleanValue) { println("if statement is not working correctly"); }
            IfStmt(
                condition = Variable("booleanValue", s()),
                thenBranch = listOf(
                    Println(LiteralString("if statement is not working correctly", s()), s()),
                ),
                elseBranch = null,
                span = s(),
            ),
            // println("outside of conditional");
            Println(LiteralString("outside of conditional", s()), s()),
        )

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(stream)

        when (res) {
            is Success -> assertEquals(listOf("outside of conditional"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error.humanReadable()}")
        }
    }
}
