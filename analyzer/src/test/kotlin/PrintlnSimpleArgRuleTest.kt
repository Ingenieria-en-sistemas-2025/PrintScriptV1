import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintlnSimpleArgRuleTest {

    private val rule = PrintlnSimpleArgRule()

    @Test
    fun printLnWithVariableOk() {
        val p = program(
            varDecl("x", Type.NUMBER, litNum("5", 1, 15), 1, 1, 1, 16),
            printlnNode(variable("x", 2, 9), 2, 1, 2, 12),
        )
        val diags = rule.check(p, AnalyzerContext(AnalyzerConfig()))
        assertTrue(diags.isEmpty())
    }

    @Test
    fun printlnWithBinaryError() {
        val expr = binary(litNum("1", 2, 10), Operator.PLUS, litNum("2", 2, 14), 2, 10, 2, 15)
        val p = program(printlnNode(expr, 2, 1, 2, 16))
        val diags = rule.check(p, AnalyzerContext(AnalyzerConfig()))
        assertEquals(1, diags.size)
        assertEquals("PS-PRINTLN-SIMPLE", diags[0].ruleId)
        assertEquals(Severity.ERROR, diags[0].severity)
        assertEquals(2, diags[0].span.start.line)
    }

    @Test
    fun inhabilitatedRuleDoesNotBreak() {
        val expr = binary(litNum("1", 1, 10), Operator.PLUS, litNum("2", 1, 14), 1, 10, 1, 15)
        val p = program(printlnNode(expr, 1, 1, 1, 16))
        val cfg = AnalyzerConfig(printlnRule = PrintlnRuleConfig(enabled = false))
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertTrue(diags.isEmpty())
    }
}
