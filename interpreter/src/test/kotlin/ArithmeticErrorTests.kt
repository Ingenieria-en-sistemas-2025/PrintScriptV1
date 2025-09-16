import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.common.Failure
import org.printscript.common.Operator
import kotlin.test.Test
import kotlin.test.assertEquals

class ArithmeticErrorTests {
    @Test
    fun `division by zero in println fails`() {
        val expr = binary(litNum("1", 1, 1), Operator.DIVIDE, litNum("0", 1, 5), 1, 1, 1, 6)
        val res = run(streamOf(printlnNode(expr, 1, 1, 1, 10)))
        assertTrue(res is Failure)
        assertEquals("DivisionByZero", (res as Failure).error::class.simpleName)
    }
}
