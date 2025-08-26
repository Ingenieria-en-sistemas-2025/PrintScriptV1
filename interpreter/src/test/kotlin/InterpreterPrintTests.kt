import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InterpreterPrintTests {
    private fun s() = Span(Position(1, 1), Position(1, 1))

    private fun makeInterpreter(): ProgramInterpreter {
        val eval = DefaultExprEvaluator()
        val actions = mapOf(
            VarDeclaration::class to VarDeclarationAction(),
            Assignment::class     to AssignmentAction(),
            Println::class        to PrintlnAction(),
        )
        val exec = StmtActionExecutor(eval, actions)  //busca action en el map
        return ProgramInterpreter(exec)
    }

    @Test
    fun `println number 42`() {
        val stmts: List<Statement> = listOf(
            Println( //aca usa la println action
                value = LiteralNumber(raw = "42", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> assertEquals(listOf("42"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare variable x = 42 and print it`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration( //aca usa la VarDeclaration action
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "42", span = s()),
                span = s()
            ),
            Println( //aca usa la println action
                value = Variable(name = "x", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> assertEquals(listOf("42"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare variable x = 42, assign x = 100 and print it`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration( //aca usa la VarDeclaration action
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "42", span = s()),
                span = s()
            ),
            Assignment( //aca usa la Assignment action
                name = "x",
                value = LiteralNumber(raw = "100", span = s()),
                span = s()
            ),
            Println( //aca usa la println action
                value = Variable(name = "x", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> {
                println("OUTPUTSSSS: ${res.value.outputs}")
                assertEquals(listOf("100"), res.value.outputs)
            }
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare variable x = 42, assign x = 100 and print x + 1`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration( //aca usa la VarDeclaration action
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "42", span = s()),
                span = s()
            ),
            Assignment( //aca usa la Assignment action
                name = "x",
                value = LiteralNumber(raw = "100", span = s()),
                span = s()
            ),
            Println( //aca usa la println action
                value = Binary(
                    left = Variable(name = "x", span = s()),
                    operator = Operator.PLUS,
                    right = LiteralNumber(raw = "1", span = s()),
                    span = s()
                ),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> {
                assertEquals(listOf("101"), res.value.outputs)
            }
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `print string "hello world"`() {
        val stmts: List<Statement> = listOf(
            Println( //aca usa la println action
                value = LiteralString(value = "hello world", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)

        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)

        when (res) {
            is Success -> assertEquals(listOf("hello world"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `declare x=10, y=20, assign x=y, print x and y`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "10", span = s()),
                span = s()
            ),
            VarDeclaration(
                name = "y",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "20", span = s()),
                span = s()
            ),
            Assignment(
                name = "x",
                value = Variable(name = "y", span = s()),
                span = s()
            ),
            Println(
                value = Variable(name = "x", span = s()),
                span = s()
            ),
            Println(
                value = Variable(name = "y", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> assertEquals(listOf("20", "20"), res.value.outputs)
            is Failure -> fail("Interpreter failed: ${res.error}")
        }
    }

    @Test
    fun `print undeclared variable should fail`() {
        val stmts: List<Statement> = listOf(
            Println(
                value = Variable(name = "z", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error}")
        }
    }

    @Test
    fun `redeclare variable should fail`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "1", span = s()),
                span = s()
            ),
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "2", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error}")
        }
    }

    @Test
    fun `unsupported binary operator should fail`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "10", span = s()),
                span = s()
            ),
            Println(
                value = Binary(
                    left = Variable(name = "x", span = s()),
                    operator = Operator.UNKNOWN,
                    right = LiteralNumber(raw = "3", span = s()),
                    span = s()
                ),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error}")
        }
    }

    @Test
    fun `division by zero should fail`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralNumber(raw = "10", span = s()),
                span = s()
            ),
            Println(
                value = Binary(
                    left = Variable(name = "x", span = s()),
                    operator = Operator.DIVIDE,
                    right = LiteralNumber(raw = "0", span = s()),
                    span = s()
                ),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error}")
        }
    }


    @Test
    fun `incompatible type should fail`() {
        val stmts: List<Statement> = listOf(
            VarDeclaration(
                name = "x",
                type = Type.NUMBER,
                initializer = LiteralString(value = "not a number", span = s()),
                span = s()
            )
        )
        val program = ProgramNode(statements = stmts)
        val res: Result<RunResult, InterpreterError> = makeInterpreter().run(program)
        when (res) {
            is Success -> fail("Interpreter should have failed, but succeeded with outputs: ${res.value.outputs}")
            is Failure -> println("Correctly failed: ${res.error}")
        }
    }

}