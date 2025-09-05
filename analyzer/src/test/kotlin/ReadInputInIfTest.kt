import kotlin.test.Test
import kotlin.test.assertEquals

class ReadInputInIfTest {
    private val rule = ReadInputSimpleArgRule()

    @Test
    fun readInputInThenBranch_compuesto_reporta() {
        val then = listOf(
            printlnNode(
                readInputExpr(
                    binary(litStr("Hi", 2, 20), Operator.PLUS, litStr("!", 2, 25), 2, 20, 2, 26),
                    2,
                    10,
                    2,
                    26,
                ),
                2,
                1,
                2,
                27,
            ),
        )
        val p = program(
            // if (1 + 2) { println(readInput("Hi" + "!")); }
            IfStmt(
                condition = binary(litNum("1", 1, 6), Operator.PLUS, litNum("2", 1, 10), 1, 6, 1, 11),
                thenBranch = then,
                elseBranch = null,
                span = span(1, 1, 2, 27),
            ),
        )

        val diags = rule.check(p, AnalyzerContext(AnalyzerConfig()))
        assertEquals(1, diags.size)
        assertEquals("PS-READINPUT-SIMPLE", diags[0].ruleId)
        assertEquals(Severity.ERROR, diags[0].severity)
    }
}
