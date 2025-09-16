import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.ast.LiteralBoolean
import org.printscript.common.Failure
import org.printscript.common.Operator
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorCoverageTests {

    @Test
    fun `invalid numeric literal triggers InvalidNumericLiteral with message`() {
        val badNum = litNum("12a", 1, 1)
        val res = run(streamOf(printlnNode(badNum, 1, 1, 1, 6)))
        assertTrue(res is Failure)
        val err = (res as Failure).error
        assertEquals("InvalidNumericLiteral", err::class.simpleName)
        assertEquals("Número inválido: '12a'", err.message)
    }

    @Test
    fun `readInput with non string prompt triggers ExpectedStringForPrompt`() {
        val decl = org.printscript.ast.ConstDeclaration("s", Type.STRING, readInputExpr(litNum("123", 1, 20), 1, 1, 1, 30), span(1, 1, 1, 40))
        val res = run(streamOf(decl), input = ListInputProvider(listOf("ignored")))
        assertTrue(res is Failure)
        val err = (res as Failure).error
        assertEquals("ExpectedStringForPrompt", err::class.simpleName)
        assertEquals("input(...) espera un prompt (string). Recibió number", err.message)
    }

    @Test
    fun `readEnv with non string var name triggers ExpectedStringForEnvName`() {
        val decl = org.printscript.ast.ConstDeclaration("v", Type.STRING, readEnvExpr(litNum("123", 1, 10), 1, 1, 1, 20), span(1, 1, 1, 30))
        val res = run(streamOf(decl))
        assertTrue(res is Failure)
        val err = (res as Failure).error
        assertEquals("ExpectedStringForEnvName", err::class.simpleName)
        assertEquals("env(...) espera un nombre (string). Recibió number", err.message)
    }

    @Test
    fun `binary with incompatible types triggers UnsupportedBinaryOp with message`() {
        val expr = binary(litStr("a", 1, 1), Operator.MULTIPLY, litStr("b", 1, 7), 1, 1, 1, 10)
        val res = run(streamOf(printlnNode(expr, 1, 1, 1, 12)))
        assertTrue(res is Failure)
        val err = (res as Failure).error
        assertEquals("UnsupportedBinaryOp", err::class.simpleName)
        assertEquals("Operación no soportada: string MULTIPLY string", err.message)
    }

    @Test
    fun `using undeclared variable triggers UndeclaredVariable with message and span`() {
        val pl = printlnNode(variable("x", 3, 5), 3, 1, 3, 10)
        val res = run(streamOf(pl))
        assertTrue(res is Failure)
        val err = (res as Failure).error
        assertEquals("UndeclaredVariable", err::class.simpleName)
        assertEquals("Variable no declarada: 'x'", err.message)
        assertEquals(3, err.span.start.line)
        assertEquals(5, err.span.start.column)
    }

    @Test
    fun `plus with boolean is unsupported`() {
        val expr = binary(LiteralBoolean(true, span(1, 1, 1, 5)), Operator.PLUS, litStr("x", 1, 9), 1, 1, 1, 10)
        val res = run(streamOf(printlnNode(expr, 1, 1, 1, 12)))
        assertTrue(res is Failure)
        assertEquals("UnsupportedBinaryOp", (res as Failure).error::class.simpleName)
    }
}
