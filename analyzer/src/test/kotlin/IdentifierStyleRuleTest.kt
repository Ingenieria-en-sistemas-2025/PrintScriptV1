
import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.AnalyzerContext
import org.printscript.analyzer.IdentifierStyle
import org.printscript.analyzer.IdentifierStyleRule
import org.printscript.analyzer.IdentifiersConfig
import org.printscript.analyzer.Severity
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdentifierStyleRuleTest {

    private val rule = IdentifierStyleRule()

    @Test
    fun camelCaseTestFails() {
        val p = program(
            varDecl("mi_var", Type.NUMBER, litNum("1", 1, 20), 1, 1, 1, 25),
            printlnNode(variable("mi_var", 2, 9), 2, 1, 2, 18),
        )
        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE, checkReferences = false),
        )

        val diags = rule.check(p, AnalyzerContext(cfg))
        assertEquals(1, diags.size)
        assertEquals("PS-ID-STYLE", diags[0].ruleId)
        assertEquals(Severity.WARNING, diags[0].severity)
        assertEquals(1, diags[0].span.start.line)
    }

    @Test
    fun snakeCaseTestOk() {
        val p = program(
            varDecl("mi_var", Type.NUMBER, null, 1, 1, 1, 15),
            printlnNode(variable("mi_var", 2, 9), 2, 1, 2, 18),
        )
        val cfg = AnalyzerConfig(identifiers = IdentifiersConfig(style = IdentifierStyle.SNAKE_CASE))
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertTrue(diags.isEmpty())
    }

    @Test
    fun customRegexAndFailsOnViolation() {
        val p = program(varDecl("Abc", Type.STRING, litStr("x", 1, 20), 1, 1, 1, 25))
        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(customRegex = "[a-z]{3,}", failOnViolation = true),
        )
        val diags = rule.check(p, AnalyzerContext(cfg))
        assertEquals(1, diags.size)
        assertEquals(Severity.ERROR, diags[0].severity)
    }
}
