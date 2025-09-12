import org.junit.jupiter.api.Assertions.assertTrue
import org.printscript.analyzer.DefaultStreamingAnalyzer
import org.printscript.analyzer.Severity
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.rules.IdentifierStyleRuleStreaming
import org.printscript.common.Success
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifierFailOnViolationTest {
    @Test
    fun customRegex_failOnViolationElevaASERROR() {
        // let Abc: string = "x"; -> no matchea [a-z]{3,}
        val s1 = varDecl("Abc", Type.STRING, litStr("x", 1, 20), 1, 1, 1, 25)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(
                customRegex = "[a-z]{3,}",
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
}
