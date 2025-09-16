import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.ast.IfStmt
import org.printscript.ast.LiteralBoolean
import org.printscript.common.Failure
import org.printscript.common.Success
import kotlin.test.Test
import kotlin.test.assertEquals

class ControlFlowIfTests {

    @Test
    fun `if true executes then branch only`() {
        val cond = LiteralBoolean(true, span(1, 1, 1, 5))
        val thenStmt = printlnNode(litStr("then", 2, 1), 2, 1, 2, 10)
        val elseStmt = printlnNode(litStr("else", 3, 1), 3, 1, 3, 10)
        val ifStmt = IfStmt(cond, listOf(thenStmt), listOf(elseStmt), span(1, 1, 3, 10))
        val res = run(streamOf(ifStmt))
        assertTrue(res is Success)
        assertEquals(listOf("then"), (res as Success).value.outputs)
    }

    @Test
    fun `if false with else executes else branch`() {
        val cond = LiteralBoolean(false, span(1, 1, 1, 6))
        val thenStmt = printlnNode(litStr("then", 2, 1), 2, 1, 2, 10)
        val elseStmt = printlnNode(litStr("else", 3, 1), 3, 1, 3, 10)
        val ifStmt = IfStmt(cond, listOf(thenStmt), listOf(elseStmt), span(1, 1, 3, 10))
        val res = run(streamOf(ifStmt))
        assertTrue(res is Success)
        assertEquals(listOf("else"), (res as Success).value.outputs)
    }

    @Test
    fun `if false without else emits nothing`() {
        val cond = LiteralBoolean(false, span(1, 1, 1, 6))
        val thenStmt = printlnNode(litStr("then", 2, 1), 2, 1, 2, 10)
        val ifStmt = IfStmt(cond, listOf(thenStmt), null, span(1, 1, 2, 10))
        val res = run(streamOf(ifStmt))
        assertTrue(res is Success)
        assertEquals(emptyList(), (res as Success).value.outputs)
    }

    @Test
    fun `if condition must be boolean`() {
        val cond = litStr("not-bool", 1, 1)
        val thenStmt = printlnNode(litStr("nope", 2, 1), 2, 1, 2, 10)
        val ifStmt = IfStmt(cond, listOf(thenStmt), null, span(1, 1, 2, 10))
        val res = run(streamOf(ifStmt))
        assertTrue(res is Failure)
        assertEquals("InternalRuntimeError", (res as Failure).error::class.simpleName)
    }
}
