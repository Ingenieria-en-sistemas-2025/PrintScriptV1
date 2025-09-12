import org.junit.jupiter.api.Assertions.assertEquals
import org.printscript.analyzer.AnalyzerFactory
import org.printscript.analyzer.config.AnalyzerConfig
import org.printscript.analyzer.config.IdentifiersConfig
import org.printscript.analyzer.config.PrintlnRuleConfig
import org.printscript.analyzer.rules.IdentifierStyle
import org.printscript.common.Operator
import org.printscript.common.Success
import org.printscript.common.Type
import org.printscript.common.Version
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultStreamingAnalyzerV0Test {

    @Test
    fun rulesOrderedAndCombinedStreaming() {
        // let mi_var: number = 1 + 2;
        // println(1 + 2);
        // println(mi_var);
        val s1 = varDecl(
            "mi_var",
            Type.NUMBER,
            binary(litNum("1", 1, 20), Operator.PLUS, litNum("2", 1, 24), 1, 20, 1, 25),
            1,
            1,
            1,
            26,
        )
        val s2 = printlnNode(
            binary(litNum("1", 2, 10), Operator.PLUS, litNum("2", 2, 14), 2, 10, 2, 15),
            2,
            1,
            2,
            16,
        )
        val s3 = printlnNode(variable("mi_var", 3, 9), 3, 1, 3, 16)

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE), // "mi_var" viola camelCase
            printlnRule = PrintlnRuleConfig(enabled = true),
        )

        val engine = AnalyzerFactory.forVersion(Version.V0)

        val out = CollectorEmitter()
        val res = engine.analyze(streamOf(s1, s2, s3), cfg, out)
        assertTrue(res is Success<Unit>, "Analyzer no debería fallar")

        // Esperamos 2 diagnósticos: naming en línea 1 y println simple en línea 2
        val diags = out.diags.sortedWith(compareBy({ it.span.start.line }, { it.span.start.column }))
        assertEquals(2, diags.size)
        assertEquals("PS-ID-STYLE", diags[0].ruleId)
        assertEquals(1, diags[0].span.start.line)
        assertEquals("PS-PRINTLN-SIMPLE", diags[1].ruleId)
        assertEquals(2, diags[1].span.start.line)
    }
}
