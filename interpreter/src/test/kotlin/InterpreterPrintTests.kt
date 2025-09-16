import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.common.Operator
import org.printscript.common.Success
import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterPrintTests {

    @Test
    fun `println of number addition formats without trailing decimals`() {
        val expr = binary(litNum("1", 1, 1), Operator.PLUS, litNum("2", 1, 5), 1, 1, 1, 6)
        val res = run(streamOf(printlnNode(expr, 1, 1, 1, 10)))
        assertTrue(res is Success)
        assertEquals(listOf("3"), (res as Success).value.outputs)
    }

    @Test
    fun `string concatenation in println`() {
        val expr = binary(litStr("Hi ", 1, 1), Operator.PLUS, litStr("Cati", 1, 7), 1, 1, 1, 14)
        val res = run(streamOf(printlnNode(expr, 1, 1, 1, 20)))
        assertTrue(res is Success)
        assertEquals(listOf("Hi Cati"), (res as Success).value.outputs)
    }

    @Test
    fun `number formatting drops trailing zeros but keeps decimals when needed`() {
        val p1 = printlnNode(litNum("3", 1, 1), 1, 1, 1, 5)
        val p2 = printlnNode(binary(litNum("7", 2, 1), Operator.DIVIDE, litNum("2", 2, 5), 2, 1, 2, 6), 2, 1, 2, 10)
        val res = run(streamOf(p1, p2))
        assertTrue(res is Success)
        assertEquals(listOf("3", "3.5"), (res as Success).value.outputs)
    }

    @Test
    fun `string plus number formats number without trailing decimals`() {
        val expr1 = binary(litStr("X=", 1, 1), Operator.PLUS, litNum("2", 1, 6), 1, 1, 1, 7)
        val expr2 = binary(litNum("4", 2, 1), Operator.PLUS, litStr(" apples", 2, 5), 2, 1, 2, 14)
        val res = run(streamOf(printlnNode(expr1, 1, 1, 1, 12), printlnNode(expr2, 2, 1, 2, 20)))
        assertTrue(res is Success)
        assertEquals(listOf("X=2", "4 apples"), (res as Success).value.outputs)
    }
}
