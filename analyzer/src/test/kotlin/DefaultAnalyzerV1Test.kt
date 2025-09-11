import org.printscript.analyzer.AnalyzerConfig
import org.printscript.analyzer.DefaultAnalyzer
import org.printscript.analyzer.IdentifierStyle
import org.printscript.analyzer.IdentifierStyleRuleOld
import org.printscript.analyzer.IdentifiersConfig
import org.printscript.analyzer.PrintlnRuleConfig
import org.printscript.analyzer.PrintlnSimpleArgRuleOld
import org.printscript.analyzer.ReadInputRuleConfig
import org.printscript.analyzer.ReadInputSimpleArgRuleOld
import org.printscript.common.Failure
import org.printscript.common.Operator
import org.printscript.common.Success
import org.printscript.common.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAnalyzerV1Test {

    @Test
    fun combinaRules_v1_yReportaReadInputCompuesto() {
        val p = program(
            // let x: string = readInput("A" + "B");
            varDecl(
                "x",
                Type.STRING,
                readInputExpr(
                    binary(litStr("A", 1, 25), Operator.PLUS, litStr("B", 1, 31), 1, 25, 1, 32),
                    1,
                    20,
                    1,
                    32,
                ),
                1,
                1,
                1,
                33,
            ),
        )

        val engine = DefaultAnalyzer(
            listOf(
                IdentifierStyleRuleOld(),
                PrintlnSimpleArgRuleOld(),
                ReadInputSimpleArgRuleOld(),
            ),
        )

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE),
            printlnRule = PrintlnRuleConfig(enabled = true),
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = false),
        )

        when (val r = engine.analyze(p, cfg)) {
            is Success -> {
                val diags = r.value.diagnostics
                assertEquals(1, diags.size)
                assertEquals("PS-READINPUT-SIMPLE", diags.first().ruleId)
            }
            is Failure -> fail("Analyzer falló: ${r.error.message}")
        }
    }
}
