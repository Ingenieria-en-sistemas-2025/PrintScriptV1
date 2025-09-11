import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.DefaultAnalyzer
import org.printscript.analyzer.IdentifierStyle
import org.printscript.analyzer.IdentifierStyleRuleOld
import org.printscript.analyzer.IdentifiersConfig
import org.printscript.analyzer.PrintlnRuleConfig
import org.printscript.analyzer.PrintlnSimpleArgRuleOld
import org.printscript.common.Failure
import org.printscript.common.Operator
import org.printscript.common.Success
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAnalyzerTest {

    @Test
    fun rulesOrderedAndCombined() {
        val p = program(
            varDecl(
                "mi_var",
                Type.NUMBER,
                binary(litNum("1", 1, 20), Operator.PLUS, litNum("2", 1, 24), 1, 20, 1, 25),
                1,
                1,
                1,
                26,
            ),
            printlnNode(
                binary(litNum("1", 2, 10), Operator.PLUS, litNum("2", 2, 14), 2, 10, 2, 15),
                2,
                1,
                2,
                16,
            ),
            printlnNode(variable("mi_var", 3, 9), 3, 1, 3, 16),
        )

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE),
            printlnRule = PrintlnRuleConfig(enabled = true),
        )
        val engine = DefaultAnalyzer(listOf(IdentifierStyleRuleOld(), PrintlnSimpleArgRuleOld()))
        val res = engine.analyze(p, cfg)
        when (res) {
            is Success -> {
                val diags = res.value.diagnostics
                assertEquals(2, diags.size)
                assertEquals("PS-ID-STYLE", diags[0].ruleId) // let mi_var...
                assertEquals(1, diags[0].span.start.line)
                assertEquals("PS-PRINTLN-SIMPLE", diags[1].ruleId) // println(1+2)
                assertEquals(2, diags[1].span.start.line)
            }
            is Failure -> fail("Analyzer falló: ${res.error.message}")
        }
    }
}
