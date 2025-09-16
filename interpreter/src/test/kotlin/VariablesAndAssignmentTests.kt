import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.common.Failure
import org.printscript.common.Success
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals

class VariablesAndAssignmentTests {

    @Test
    fun `var declaration with init + println variable`() {
        val decl = varDecl("x", Type.NUMBER, litNum("5", 1, 15), 1, 1, 1, 16)
        val out = printlnNode(variable("x", 2, 1), 2, 1, 2, 10)
        val res = run(streamOf(decl, out))
        assertTrue(res is Success)
        assertEquals(listOf("5"), (res as Success).value.outputs)
    }

    @Test
    fun `type mismatch on assignment fails`() {
        val d = varDecl("x", Type.NUMBER, null, 1, 1, 1, 10)
        val a = assignment("x", litStr("oops", 2, 10), 2, 1, 2, 20)
        val res = run(streamOf(d, a))
        assertTrue(res is Failure)
        assertEquals("IncompatibleType", (res as Failure).error::class.simpleName)
    }

    @Test
    fun `const cannot be reassigned`() {
        val c = org.printscript.ast.ConstDeclaration("x", Type.NUMBER, litNum("1", 1, 10), span(1, 1, 1, 12))
        val a = assignment("x", litNum("2", 2, 10), 2, 1, 2, 12)
        val res = run(streamOf(c, a))
        assertTrue(res is Failure)
        assertEquals("ConstAssignment", (res as Failure).error::class.simpleName)
    }

    @Test
    fun `var reassign updates value`() {
        val d = varDecl("x", Type.NUMBER, litNum("1", 1, 10), 1, 1, 1, 12)
        val a = assignment("x", litNum("7", 2, 10), 2, 1, 2, 12)
        val p = printlnNode(variable("x", 3, 1), 3, 1, 3, 10)
        val res = run(streamOf(d, a, p))
        assertTrue(res is Success)
        assertEquals(listOf("7"), (res as Success).value.outputs)
    }
}
