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
                IdentifierStyleRule(),
                PrintlnSimpleArgRule(),
                ReadInputSimpleArgRule(),
            ),
        )

        val cfg = AnalyzerConfig(
            identifiers = IdentifiersConfig(style = IdentifierStyle.CAMEL_CASE),
            printlnRule = PrintlnRuleConfig(enabled = true),
            readInputRule = ReadInputRuleConfig(enabled = true, onlyStringLiteralOrIdentifier = false),
        )

        when (val r = engine.analize(p, cfg)) {
            is Success -> {
                val diags = r.value.diagnostics
                assertEquals(1, diags.size)
                assertEquals("PS-READINPUT-SIMPLE", diags.first().ruleId)
            }
            is Failure -> fail("Analyzer falló: ${r.error.message}")
        }
    }
}
