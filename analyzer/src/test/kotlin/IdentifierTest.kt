import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.DefaultStreamingAnalyzer
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.rules.IdentifierStyle
import org.printscript.analyzer.rules.IdentifierStyleRuleStreaming
import org.printscript.common.Success
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifierTest {

    @Test
    fun snakeCaseOkNoDiagnostics() {
        // let mi_var: string = "x";
        val s1 = varDecl("mi_var", Type.STRING, litStr("x", 1, 20), 1, 1, 1, 25)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(
                style = IdentifierStyle.SNAKE_CASE,
                failOnViolation = false,
            ),
        )

        val engine = DefaultStreamingAnalyzer(listOf(IdentifierStyleRuleStreaming()))
        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1), cfg, out)

        assertTrue(res is Success<Unit>)
        assertEquals(0, out.diags.size)
    }

    @Test
    fun camelCaseFailOnViolationElevaASERROR() {
        val s1 = varDecl("a_b", Type.STRING, litStr("x", 1, 20), 1, 1, 1, 25)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(
                enabled = true,
                failOnViolation = true,
            ),
        )

        val engine = DefaultStreamingAnalyzer(listOf(IdentifierStyleRuleStreaming()))
        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1), cfg, out)

        assertTrue(res is Success<Unit>)
        assertEquals(1, out.diags.size)
        assertEquals("PS-ID-STYLE", out.diags[0].ruleId)
        assertEquals(Severity.ERROR, out.diags[0].severity)
    }

    @Test
    fun assignmentNameIsChecked() {
        val s1 = assignment("a_b", litNum("1", 1, 10), 1, 1, 1, 12)

        val cfg = AnalyzerConfig(identifiers = IdentifiersConfig(enabled = true, failOnViolation = true))
        val engine = DefaultStreamingAnalyzer(listOf(IdentifierStyleRuleStreaming()))
        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1), cfg, out)

        assertTrue(res is Success<Unit>)
        assertEquals(1, out.diags.size)
        assertEquals("PS-ID-STYLE", out.diags[0].ruleId)
        assertEquals(Severity.ERROR, out.diags[0].severity)
    }

    @Test
    fun checkReferencesTrueMarksUsagesInOtherStatements() {
        val s1 = varDecl("a_b", Type.STRING, litStr("x", 1, 20), 1, 1, 1, 25)
        val s2 = printlnNode(variable("a_b", 2, 9), 2, 1, 2, 18)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(
                enabled = true,
                // CAMEL esperado → 'a_b' inválido tanto en declaración como en uso
                checkReferences = true,
                failOnViolation = false,
            ),
        )

        val engine = DefaultStreamingAnalyzer(listOf(IdentifierStyleRuleStreaming()))
        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1, s2), cfg, out)

        assertTrue(res is Success<Unit>)
        assertEquals(2, out.diags.size)
        out.diags.sortBy { it.span.start.line }
        assertEquals(1, out.diags[0].span.start.line)
        assertEquals(2, out.diags[1].span.start.line)
    }
}
