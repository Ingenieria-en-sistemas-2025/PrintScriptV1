import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.AnalyzerContext
import org.printscript.analyzer.ReadInputRuleConfig
import org.printscript.analyzer.ReadInputSimpleArgRule
import org.printscript.analyzer.Severity
import org.printscript.common.Operator
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadInputSimpleArgRuleTest {
    private val rule = ReadInputSimpleArgRule()

    @Test
    fun readInputConLiteralString_ok() {
        val p = program(
            printlnNode(
                readInputExpr(litStr("Name: ", 1, 20), 1, 10, 1, 30),
                1,
                1,
                1,
                31,
            ),
        )
        val cfg = AnalyzerConfig(
            // por defecto enabled=true, strict=false
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = false),
        )
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertTrue(diags.isEmpty())
    }

    @Test
    fun readInputConIdentificador_ok() {
        val p = program(
            varDecl("prompt", Type.STRING, litStr(">> ", 1, 20), 1, 1, 1, 24),
            printlnNode(readInputExpr(variable("prompt", 2, 12), 2, 10, 2, 19), 2, 1, 2, 20),
        )
        val diags = rule.check(p, AnalyzerContext(AnalyzerConfig()))
        assertTrue(diags.isEmpty())
    }

    @Test
    fun readInputConExpresionCompuesta_error() {
        // readInput("Hi " + "there")
        val expr = readInputExpr(
            binary(litStr("Hi ", 1, 20), Operator.PLUS, litStr("there", 1, 26), 1, 20, 1, 31),
            1,
            10,
            1,
            31,
        )
        val p = program(printlnNode(expr, 1, 1, 1, 32))

        val diags = rule.check(p, AnalyzerContext(AnalyzerConfig()))
        assertEquals(1, diags.size)
        assertEquals("PS-READINPUT-SIMPLE", diags[0].ruleId)
        assertEquals(Severity.ERROR, diags[0].severity)
        assertEquals(1, diags[0].span.start.line) // span del prompt problemático
    }

    @Test
    fun modoEstricto_rechazaNumeroLiteral() {
        // strict: solo identificador o string literal (no number)
        val cfg = AnalyzerConfig(
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = true),
        )
        val p = program(
            printlnNode(readInputExpr(litNum("123", 1, 20), 1, 10, 1, 23), 1, 1, 1, 24),
        )
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertEquals(1, diags.size)
        assertEquals("PS-READINPUT-SIMPLE", diags[0].ruleId)
        assertEquals(Severity.ERROR, diags[0].severity)
    }

    @Test
    fun reglaDeshabilitada_noReporta() {
        val cfg = AnalyzerConfig(
            readInputRule = ReadInputRuleConfig(enabled = false),
        )
        val p = program(
            printlnNode(
                readInputExpr(
                    binary(litStr("A", 1, 20), Operator.PLUS, litStr("B", 1, 24), 1, 20, 1, 25),
                    1,
                    10,
                    1,
                    25,
                ),
                1,
                1,
                1,
                26,
            ),
        )
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertTrue(diags.isEmpty())
    }
}
